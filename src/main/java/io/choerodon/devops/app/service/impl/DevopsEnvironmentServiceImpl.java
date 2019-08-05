package io.choerodon.devops.app.service.impl;

import java.util.*;
import java.util.stream.Collectors;

import com.alibaba.fastjson.JSONArray;
import com.github.pagehelper.PageInfo;
import com.google.gson.Gson;
import io.choerodon.asgard.saga.annotation.Saga;
import io.choerodon.asgard.saga.producer.StartSagaBuilder;
import io.choerodon.asgard.saga.producer.TransactionalProducer;
import io.choerodon.base.domain.PageRequest;
import io.choerodon.core.exception.CommonException;
import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.devops.api.validator.DevopsEnvironmentValidator;
import io.choerodon.devops.api.vo.*;
import io.choerodon.devops.api.vo.iam.UserVO;
import io.choerodon.devops.app.eventhandler.constants.SagaTopicCodeConstants;
import io.choerodon.devops.app.eventhandler.payload.DevopsEnvUserPayload;
import io.choerodon.devops.app.eventhandler.payload.EnvGitlabProjectPayload;
import io.choerodon.devops.app.service.*;
import io.choerodon.devops.infra.dto.*;
import io.choerodon.devops.infra.dto.gitlab.CommitDTO;
import io.choerodon.devops.infra.dto.gitlab.GitlabProjectDTO;
import io.choerodon.devops.infra.dto.gitlab.MemberDTO;
import io.choerodon.devops.infra.dto.gitlab.ProjectHookDTO;
import io.choerodon.devops.infra.dto.iam.IamUserDTO;
import io.choerodon.devops.infra.dto.iam.OrganizationDTO;
import io.choerodon.devops.infra.dto.iam.ProjectDTO;
import io.choerodon.devops.infra.enums.AccessLevel;
import io.choerodon.devops.infra.enums.EnvironmentGitopsStatus;
import io.choerodon.devops.infra.enums.HelmObjectKind;
import io.choerodon.devops.infra.enums.InstanceStatus;
import io.choerodon.devops.infra.feign.operator.GitlabServiceClientOperator;
import io.choerodon.devops.infra.handler.ClusterConnectionHandler;
import io.choerodon.devops.infra.mapper.DevopsEnvFileErrorMapper;
import io.choerodon.devops.infra.mapper.DevopsEnvUserPermissionMapper;
import io.choerodon.devops.infra.mapper.DevopsEnvironmentMapper;
import io.choerodon.devops.infra.util.*;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * Created by younger on 2018/4/9.
 */
@Service
public class DevopsEnvironmentServiceImpl implements DevopsEnvironmentService {

    private static final Gson gson = new Gson();
    private static final String MEMBER = "member";
    private static final String OWNER = "owner";
    private static final String MASTER = "master";
    private static final String README = "README.md";
    private static final String README_CONTENT =
            "# This is gitops env repository!";
    private static final String ENV = "ENV";
    private static final String PROJECT_OWNER = "role/project/default/project-owner";
    private static final String PROJECT_MEMBER = "role/project/default/project-member";

    @Value("${agent.version}")
    private String agentExpectVersion;

    @Value("${agent.serviceUrl}")
    private String agentServiceUrl;

    @Value("${agent.repoUrl}")
    private String agentRepoUrl;

    @Value("${services.gateway.url}")
    private String gatewayUrl;

    @Value("${services.gitlab.sshUrl}")
    private String gitlabSshUrl;

    @Value("${services.gitlab.url}")
    private String gitlabUrl;

    @Autowired
    private DevopsServiceService devopsServiceService;
    @Autowired
    private DevopsEnvironmentValidator devopsEnvironmentValidator;
    @Autowired
    private AgentCommandService agentCommandService;
    @Autowired
    private DevopsEnvironmentMapper devopsEnvironmentMapper;
    @Autowired
    private IamService iamService;
    @Autowired
    private UserAttrService userAttrService;
    @Autowired
    private DevopsProjectService devopsProjectService;
    @Autowired
    private DevopsEnvGroupService devopsEnvGroupService;
    @Autowired
    private DevopsClusterService devopsClusterService;
    @Autowired
    private DevopsEnvUserPermissionService devopsEnvUserPermissionService;
    @Autowired
    private DevopsEnvUserPermissionMapper devopsEnvUserPermissionMapper;
    @Autowired
    private GitlabServiceClientOperator gitlabServiceClientOperator;
    @Autowired
    private DevopsEnvCommitService devopsEnvCommitService;
    @Autowired
    private AppServiceInstanceService appServiceInstanceService;
    @Autowired
    private DevopsEnvFileErrorMapper devopsEnvFileErrorMapper;
    @Autowired
    private DevopsEnvCommandService devopsEnvCommandService;
    @Autowired
    private DevopsIngressService devopsIngressService;
    @Autowired
    private TransactionalProducer producer;
    @Autowired
    private GitlabGroupMemberService gitlabGroupMemberService;
    @Autowired
    private DevopsGitService devopsGitService;
    @Autowired
    private ClusterConnectionHandler clusterConnectionHandler;

    @Override
    @Saga(code = SagaTopicCodeConstants.DEVOPS_CREATE_ENV, description = "创建环境", inputSchema = "{}")
    @Transactional(rollbackFor = Exception.class)
    public void create(Long projectId, DevopsEnvironmentVO devopsEnvironmentVO) {
        DevopsEnvironmentDTO devopsEnvironmentDTO = ConvertUtils.convertObject(devopsEnvironmentVO, DevopsEnvironmentDTO.class);
        // 创建环境时默认跳过权限校验
        devopsEnvironmentDTO.setSkipCheckPermission(Boolean.TRUE);
        devopsEnvironmentDTO.setProjectId(projectId);
        checkCode(projectId, devopsEnvironmentVO.getClusterId(), devopsEnvironmentVO.getCode());
        devopsEnvironmentDTO.setActive(true);
        devopsEnvironmentDTO.setConnected(false);
        devopsEnvironmentDTO.setSynchro(false);
        devopsEnvironmentDTO.setFailed(false);
        devopsEnvironmentDTO.setClusterId(devopsEnvironmentVO.getClusterId());
        devopsEnvironmentDTO.setToken(GenerateUUID.generateUUID());
        devopsEnvironmentDTO.setProjectId(projectId);
        ProjectDTO projectDTO = iamService.queryIamProject(projectId);
        OrganizationDTO organizationDTO = iamService.queryOrganizationById(projectDTO.getOrganizationId());

        UserAttrDTO userAttrDTO = userAttrService.baseQueryById(TypeUtil.objToLong(GitUserNameUtil.getUserId()));
        if (userAttrDTO == null) {
            throw new CommonException("error.gitlab.user.sync.failed");
        }

        // 查询创建应用所在的gitlab应用组
        DevopsProjectDTO devopsProjectDTO = devopsProjectService.baseQueryByProjectId(projectId);
        MemberDTO memberDTO = gitlabServiceClientOperator.queryGroupMember(
                TypeUtil.objToInteger(devopsProjectDTO.getDevopsEnvGroupId()),
                TypeUtil.objToInteger(userAttrDTO.getGitlabUserId()));
        if (memberDTO == null || memberDTO.getAccessLevel().equals(AccessLevel.OWNER.toValue())) {
            throw new CommonException("error.user.not.owner");
        }

        List<String> sshKeys = FileUtil.getSshKey(
                organizationDTO.getCode() + "/" + projectDTO.getCode() + "/" + devopsEnvironmentVO.getCode());
        devopsEnvironmentDTO.setEnvIdRsa(sshKeys.get(0));
        devopsEnvironmentDTO.setEnvIdRsaPub(sshKeys.get(1));
        Long envId = baseCreate(devopsEnvironmentDTO).getId();
        devopsEnvironmentDTO.setId(envId);

        EnvGitlabProjectPayload gitlabProjectPayload = new EnvGitlabProjectPayload();
        gitlabProjectPayload.setGroupId(TypeUtil.objToInteger(devopsProjectDTO.getDevopsEnvGroupId()));
        gitlabProjectPayload.setUserId(TypeUtil.objToInteger(userAttrDTO.getGitlabUserId()));
        gitlabProjectPayload.setPath(devopsEnvironmentVO.getCode());
        gitlabProjectPayload.setOrganizationId(null);
        gitlabProjectPayload.setType(ENV);
        IamUserDTO iamUserDTO = iamService.queryUserByUserId(userAttrDTO.getIamUserId());
        gitlabProjectPayload.setLoginName(iamUserDTO.getLoginName());
        gitlabProjectPayload.setRealName(iamUserDTO.getRealName());
        gitlabProjectPayload.setClusterId(devopsEnvironmentVO.getClusterId());
        gitlabProjectPayload.setIamProjectId(projectId);
        gitlabProjectPayload.setSkipCheckPermission(devopsEnvironmentDTO.getSkipCheckPermission());

        producer.apply(
                StartSagaBuilder
                        .newBuilder()
                        .withLevel(ResourceLevel.PROJECT)
                        .withSagaCode(SagaTopicCodeConstants.DEVOPS_CREATE_ENV)
                        .withPayloadAndSerialize(gitlabProjectPayload)
                        .withRefId("")
                        .withRefType("")
                        .withSourceId(projectId),
                builder -> {
                }
        );
        agentCommandService.initEnv(devopsEnvironmentDTO, devopsEnvironmentVO.getClusterId());
    }


    @Override
    public List<DevopsEnvGroupEnvsVO> listDevopsEnvGroupEnvs(Long projectId, Boolean active) {
        List<DevopsEnvGroupEnvsVO> devopsEnvGroupEnvsDTOS = new ArrayList<>();
        List<Long> connectedClusterList = clusterConnectionHandler.getConnectedEnvList();
        List<Long> upgradeClusterList = clusterConnectionHandler.getUpdatedEnvList();
        List<DevopsEnvironmentDTO> devopsEnvironmentDTOS = baseListByProjectIdAndActive(projectId, active).stream().peek(t ->
                setEnvStatus(connectedClusterList, upgradeClusterList, t)
        )
                .collect(Collectors.toList());
        List<DevopsEnviromentRepVO> devopsEnviromentRepDTOS = ConvertUtils.convertList(devopsEnvironmentDTOS, DevopsEnviromentRepVO.class);
        if (!active) {
            DevopsEnvGroupEnvsVO devopsEnvGroupEnvsDTO = new DevopsEnvGroupEnvsVO();
            devopsEnvGroupEnvsDTO.setDevopsEnviromentRepDTOs(devopsEnviromentRepDTOS);
            devopsEnvGroupEnvsDTOS.add(devopsEnvGroupEnvsDTO);
            return devopsEnvGroupEnvsDTOS;
        }
        List<DevopsEnvGroupDTO> devopsEnvGroupES = devopsEnvGroupService.baseListByProjectId(projectId);
        devopsEnviromentRepDTOS.forEach(devopsEnviromentRepDTO -> {
            DevopsClusterDTO devopsClusterDTO = devopsClusterService.baseQuery(devopsEnviromentRepDTO.getClusterId());
            devopsEnviromentRepDTO.setClusterName(devopsClusterDTO == null ? null : devopsClusterDTO.getName());
            if (devopsEnviromentRepDTO.getDevopsEnvGroupId() == null) {
                devopsEnviromentRepDTO.setDevopsEnvGroupId(0L);
            }
        });
        //按照环境组分组查询，有环境的环境组放前面，没环境的环境组放后面
        Map<Long, List<DevopsEnviromentRepVO>> resultMaps = devopsEnviromentRepDTOS.stream()
                .collect(Collectors.groupingBy(DevopsEnviromentRepVO::getDevopsEnvGroupId));
        List<Long> envGroupIds = new ArrayList<>();
        resultMaps.forEach((key, value) -> {
            envGroupIds.add(key);
            DevopsEnvGroupEnvsVO devopsEnvGroupEnvsDTO = new DevopsEnvGroupEnvsVO();
            DevopsEnvGroupDTO devopsEnvGroupDTO = new DevopsEnvGroupDTO();
            if (key != 0) {
                devopsEnvGroupDTO = devopsEnvGroupService.baseQuery(key);
            }
            devopsEnvGroupEnvsDTO.setDevopsEnvGroupId(devopsEnvGroupDTO.getId());
            devopsEnvGroupEnvsDTO.setDevopsEnvGroupName(devopsEnvGroupDTO.getName());
            devopsEnvGroupEnvsDTO.setDevopsEnviromentRepDTOs(value);
            devopsEnvGroupEnvsDTOS.add(devopsEnvGroupEnvsDTO);
        });
        devopsEnvGroupES.forEach(devopsEnvGroupE -> {
            if (!envGroupIds.contains(devopsEnvGroupE.getId())) {
                DevopsEnvGroupEnvsVO devopsEnvGroupEnvsDTO = new DevopsEnvGroupEnvsVO();
                devopsEnvGroupEnvsDTO.setDevopsEnvGroupId(devopsEnvGroupE.getId());
                devopsEnvGroupEnvsDTO.setDevopsEnvGroupName(devopsEnvGroupE.getName());
                devopsEnvGroupEnvsDTOS.add(devopsEnvGroupEnvsDTO);
            }
        });
        return devopsEnvGroupEnvsDTOS;
    }

    @Override
    public List<DevopsEnviromentRepVO> listByProjectIdAndActive(Long projectId, Boolean active) {

        // 查询当前用户的环境权限
        List<Long> permissionEnvIds = devopsEnvUserPermissionService
                .baseListByUserId(TypeUtil.objToLong(GitUserNameUtil.getUserId())).stream()
                .filter(DevopsEnvUserPermissionDTO::getPermitted)
                .map(DevopsEnvUserPermissionDTO::getEnvId).collect(Collectors.toList());
        ProjectDTO projectDTO = iamService.queryIamProject(projectId);
        // 查询当前用户是否为项目所有者
        Boolean isProjectOwner = iamService
                .isProjectOwner(TypeUtil.objToLong(GitUserNameUtil.getUserId()), projectDTO);

        List<Long> connectedClusterList = clusterConnectionHandler.getConnectedEnvList();
        List<Long> upgradeClusterList = clusterConnectionHandler.getUpdatedEnvList();
        List<DevopsEnvironmentDTO> devopsEnvironmentDTOS = baseListByProjectIdAndActive(projectId, active).stream()
                .filter(devopsEnvironmentE -> !devopsEnvironmentE.getFailed()).peek(t -> {
                    setEnvStatus(connectedClusterList, upgradeClusterList, t);
                    // 项目成员返回拥有对应权限的环境，项目所有者返回所有环境
                    setPermission(t, permissionEnvIds, isProjectOwner);
                })
                .collect(Collectors.toList());
        return ConvertUtils.convertList(devopsEnvironmentDTOS, DevopsEnviromentRepVO.class);
    }

    @Override
    public List<DevopsEnviromentRepVO> listDeployed(Long projectId) {
        List<Long> envList = devopsServiceService.baseListEnvByRunningService();
        return listByProjectIdAndActive(projectId, true).stream().filter(t ->
                envList.contains(t.getId())).collect(Collectors.toList());
    }

    @Override
    public List<DevopsEnvironmentViewVO> listInstanceEnvTree(Long projectId) {
        List<Long> upgradeClusterList = clusterConnectionHandler.getUpdatedEnvList();

        List<DevopsEnvironmentViewVO> connectedEnvs = new ArrayList<>();
        List<DevopsEnvironmentViewVO> unConnectedEnvs = new ArrayList<>();
        List<DevopsEnvironmentViewVO> unSynchronizedEnvs = new ArrayList<>();

        devopsEnvironmentMapper.listInstanceEnvTree(projectId).forEach(e -> {
            // 将DTO层对象转为VO
            DevopsEnvironmentViewVO vo = new DevopsEnvironmentViewVO();
            BeanUtils.copyProperties(e, vo, "apps");
            boolean connected = upgradeClusterList.contains(e.getClusterId());
            vo.setConnect(connected);
            vo.setApps(e.getApps().stream().map(app -> {

                DevopsAppServiceViewVO appVO = new DevopsAppServiceViewVO();
                BeanUtils.copyProperties(app, appVO, "instances");
                appVO.setInstances(app.getInstances().stream().map(ins -> {
                    DevopsAppServiceInstanceViewVO insVO = new DevopsAppServiceInstanceViewVO();
                    BeanUtils.copyProperties(ins, insVO);
                    return insVO;
                }).collect(Collectors.toList()));

                return appVO;
            }).collect(Collectors.toList()));

            if (connected) {
                connectedEnvs.add(vo);
            } else {
                if (Boolean.TRUE.equals(vo.getSynchronize())) {
                    unConnectedEnvs.add(vo);
                } else {
                    unSynchronizedEnvs.add(vo);
                }
            }
        });

        // 为了将环境按照状态排序: 连接（运行中） > 未连接 > 处理中（未同步完成的）
        connectedEnvs.addAll(unConnectedEnvs);
        connectedEnvs.addAll(unSynchronizedEnvs);
        return connectedEnvs;
    }

    @Override
    public List<DevopsResourceEnvOverviewVO> listResourceEnvTree(Long projectId) {
        List<Long> upgradeClusterList = clusterConnectionHandler.getUpdatedEnvList();

        List<DevopsResourceEnvOverviewVO> connectedEnvs = new ArrayList<>();
        List<DevopsResourceEnvOverviewVO> unConnectedEnvs = new ArrayList<>();
        List<DevopsResourceEnvOverviewVO> unSynchronizedEnvs = new ArrayList<>();

        devopsEnvironmentMapper.listResourceEnvTree(projectId).forEach(e -> {
            // 将DTO层对象转为VO
            DevopsResourceEnvOverviewVO vo = new DevopsResourceEnvOverviewVO();
            BeanUtils.copyProperties(e, vo);
            boolean connected = upgradeClusterList.contains(e.getClusterId());
            vo.setConnect(connected);

            if (connected) {
                connectedEnvs.add(vo);
            } else {
                if (Boolean.TRUE.equals(vo.getSynchronize())) {
                    unConnectedEnvs.add(vo);
                } else {
                    unSynchronizedEnvs.add(vo);
                }
            }
        });

        // 为了将环境按照状态排序: 连接（运行中） > 未连接 > 处理中（未同步完成的）
        connectedEnvs.addAll(unConnectedEnvs);
        connectedEnvs.addAll(unSynchronizedEnvs);
        return connectedEnvs;
    }

    @Override
    public Boolean updateActive(Long projectId, Long environmentId, Boolean active) {
        DevopsEnvironmentDTO devopsEnvironmentDTO = baseQueryById(environmentId);

        List<Long> upgradeClusterList = clusterConnectionHandler.getUpdatedEnvList();
        List<Long> connectedClusterList = clusterConnectionHandler.getConnectedEnvList();
        setEnvStatus(connectedClusterList, upgradeClusterList, devopsEnvironmentDTO);
        if (!active && devopsEnvironmentDTO.getConnected()) {
            devopsEnvironmentValidator.checkEnvCanDisabled(environmentId);
        }

        devopsEnvironmentDTO.setActive(active);
        baseUpdate(devopsEnvironmentDTO);
        return true;
    }

    @Override
    public DevopsEnvironmentUpdateVO query(Long environmentId) {
        return ConvertUtils.convertObject(baseQueryById(environmentId), DevopsEnvironmentUpdateVO.class);
    }

    @Override
    public DevopsEnvironmentInfoVO queryInfoById(Long environmentId) {
        DevopsEnvironmentInfoDTO envInfo = devopsEnvironmentMapper.queryInfoById(environmentId);
        if (envInfo == null) {
            return null;
        }

        DevopsEnvironmentInfoVO vo = new DevopsEnvironmentInfoVO();
        BeanUtils.copyProperties(envInfo, vo);

        List<Long> upgradeClusterList = clusterConnectionHandler.getUpdatedEnvList();
        vo.setConnect(upgradeClusterList.contains(envInfo.getClusterId()));

        if (envInfo.getAgentSyncCommit().equals(envInfo.getSagaSyncCommit()) &&
                envInfo.getAgentSyncCommit().equals(envInfo.getDevopsSyncCommit())) {
            vo.setGitopsStatus(EnvironmentGitopsStatus.FINISHED.getValue());
        } else {
            if (devopsEnvFileErrorMapper.queryErrorFileCountByEnvId(environmentId) > 0) {
                vo.setGitopsStatus(EnvironmentGitopsStatus.FAILED.getValue());
            } else {
                vo.setGitopsStatus(EnvironmentGitopsStatus.PROCESSING.getValue());
            }
        }
        return vo;
    }

    @Override
    public DevopsEnvResourceCountVO queryEnvResourceCount(Long environmentId) {
        return devopsEnvironmentMapper.queryEnvResourceCount(environmentId);
    }


    @Override
    public void checkEnv(DevopsEnvironmentDTO devopsEnvironmentDTO, UserAttrDTO userAttrDTO) {
        //校验用户是否有环境的权限
        devopsEnvUserPermissionService.baseCheckEnvDeployPermission(TypeUtil.objToLong(GitUserNameUtil.getUserId()), devopsEnvironmentDTO.getId());

        //校验环境是否连接
        clusterConnectionHandler.checkEnvConnection(devopsEnvironmentDTO.getClusterId());


        //检验gitops库是否存在，校验操作人是否是有gitops库的权限
        gitlabGroupMemberService.checkEnvProject(devopsEnvironmentDTO, userAttrDTO);
    }


    @Override
    public DevopsEnvironmentUpdateVO update(DevopsEnvironmentUpdateVO devopsEnvironmentUpdateDTO, Long projectId) {
        DevopsEnvironmentDTO environmentDTO = ConvertUtils.convertObject(
                devopsEnvironmentUpdateDTO, DevopsEnvironmentDTO.class);
        Long clusterId = baseQueryById(devopsEnvironmentUpdateDTO.getId()).getClusterId();
        environmentDTO.setClusterId(clusterId);
        environmentDTO.setProjectId(projectId);
        DevopsEnvironmentDTO devopsEnvironmentDTO = baseQueryById(devopsEnvironmentUpdateDTO.getId());

        if (devopsEnvironmentUpdateDTO.getClusterId() != null && !devopsEnvironmentUpdateDTO.getClusterId()
                .equals(devopsEnvironmentDTO.getClusterId())) {
            agentCommandService.initCluster(devopsEnvironmentUpdateDTO.getClusterId());
        }
        return ConvertUtils.convertObject(baseUpdate(
                environmentDTO), DevopsEnvironmentUpdateVO.class);
    }

    private void setEnvStatus(List<Long> connectedEnvList, List<Long> upgradeEnvList, DevopsEnvironmentDTO t) {
        if (connectedEnvList.contains(t.getClusterId()) && upgradeEnvList.contains(t.getClusterId())) {
            t.setConnected(true);
        } else {
            t.setConnected(false);
        }
    }


    @Override
    public void retryGitOps(Long envId) {
        DevopsEnvironmentDTO devopsEnvironmentDTO = baseQueryById(envId);
        UserAttrDTO userAttrDTO = userAttrService.baseQueryById(GitUserNameUtil.getUserId().longValue());
        if (userAttrDTO == null) {
            throw new CommonException("error.gitlab.user.sync.failed");
        }
        CommitDTO commitDO = gitlabServiceClientOperator.listCommits(devopsEnvironmentDTO.getGitlabEnvProjectId().intValue(), userAttrDTO.getGitlabUserId().intValue(), 1, 1).get(0);
        PushWebHookVO pushWebHookVO = new PushWebHookVO();
        pushWebHookVO.setCheckoutSha(commitDO.getId());
        pushWebHookVO.setUserId(userAttrDTO.getGitlabUserId().intValue());
        pushWebHookVO.setProjectId(devopsEnvironmentDTO.getGitlabEnvProjectId().intValue());
        CommitVO commitDTO = new CommitVO();
        commitDTO.setId(commitDO.getId());
        commitDTO.setTimestamp(commitDO.getTimestamp());
        pushWebHookVO.setCommits(Arrays.asList(commitDTO));

        //当环境总览第一阶段为空，第一阶段的commit不是最新commit, 第一阶段和第二阶段commit不一致时，可以重新触发gitOps
        if (devopsEnvironmentDTO.getSagaSyncCommit() == null) {
            devopsGitService.fileResourceSyncSaga(pushWebHookVO, devopsEnvironmentDTO.getToken());
        } else {
            DevopsEnvCommitDTO sagaSyncCommit = devopsEnvCommitService.baseQuery(devopsEnvironmentDTO.getSagaSyncCommit());
            if (!devopsEnvironmentDTO.getSagaSyncCommit().equals(devopsEnvironmentDTO.getDevopsSyncCommit()) || !sagaSyncCommit.getCommitSha().equals(commitDO.getId())) {
                devopsGitService.fileResourceSyncSaga(pushWebHookVO, devopsEnvironmentDTO.getToken());
            }
        }
    }

    @Override
    public void checkCode(Long projectId, Long clusterId, String code) {
        DevopsEnvironmentDTO devopsEnvironmentDTO = new DevopsEnvironmentDTO();
        DevopsClusterDTO devopsClusterDTO = devopsClusterService.baseQuery(clusterId);
        devopsEnvironmentDTO.setProjectId(projectId);
        devopsEnvironmentDTO.setClusterId(clusterId);
        devopsEnvironmentDTO.setCode(code);
        if (devopsClusterDTO.getNamespaces() != null) {
            JSONArray.parseArray(devopsClusterDTO.getNamespaces(), String.class).forEach(namespace -> {
                if (namespace.equals(code)) {
                    throw new CommonException("error.code.exist");
                }
            });
        }
        baseCheckCode(devopsEnvironmentDTO);
    }

    @Override
    public List<DevopsEnviromentRepVO> listByProjectId(Long projectId, Long appId) {
        List<DevopsEnviromentRepVO> devopsEnviromentRepDTOList = listByProjectIdAndActive(projectId, true);

        if (appId == null) {
            return devopsEnviromentRepDTOList.stream().filter(t ->
                    appServiceInstanceService.baseListByEnvId(t.getId()).stream()
                            .anyMatch(applicationInstanceDTO ->
                                    applicationInstanceDTO.getStatus().equals(InstanceStatus.RUNNING.getStatus())))
                    .collect(Collectors.toList());
        } else {
            return devopsEnviromentRepDTOList.stream().filter(t ->
                    appServiceInstanceService.baseListByEnvId(t.getId()).stream()
                            .anyMatch(applicationInstanceDTO ->
                                    applicationInstanceDTO.getStatus().equals(InstanceStatus.RUNNING.getStatus())
                                            && applicationInstanceDTO.getAppServiceId().equals(appId)))
                    .collect(Collectors.toList());
        }
    }

    @Override
    public void handleCreateEnvSaga(EnvGitlabProjectPayload gitlabProjectPayload) {
        DevopsProjectDTO gitlabGroupE = devopsProjectService.baseQueryByGitlabEnvGroupId(
                TypeUtil.objToInteger(gitlabProjectPayload.getGroupId()));
        DevopsEnvironmentDTO devopsEnvironmentDTO = baseQueryByClusterIdAndCode(gitlabProjectPayload.getClusterId(), gitlabProjectPayload.getPath());
        ProjectDTO projectDTO = iamService.queryIamProject(gitlabGroupE.getIamProjectId());
        OrganizationDTO organizationDTO = iamService.queryOrganizationById(projectDTO.getOrganizationId());

        GitlabProjectDTO gitlabProjectDO = gitlabServiceClientOperator.queryProjectByName(organizationDTO.getCode()
                + "-" + projectDTO.getCode() + "-gitops", devopsEnvironmentDTO.getCode(), gitlabProjectPayload.getUserId());
        if (gitlabProjectDO.getId() == null) {
            gitlabProjectDO = gitlabServiceClientOperator.createProject(
                    gitlabProjectPayload.getGroupId(),
                    gitlabProjectPayload.getPath(),
                    gitlabProjectPayload.getUserId(),
                    false);
        }
        devopsEnvironmentDTO.setGitlabEnvProjectId(TypeUtil.objToLong(gitlabProjectDO.getId()));
        if (gitlabServiceClientOperator.listDeployKey(gitlabProjectDO.getId(), gitlabProjectPayload.getUserId()).isEmpty()) {
            gitlabServiceClientOperator.createDeployKey(
                    gitlabProjectDO.getId(),
                    gitlabProjectPayload.getPath(),
                    devopsEnvironmentDTO.getEnvIdRsaPub(),
                    true,
                    gitlabProjectPayload.getUserId());
        }
        ProjectHookDTO projectHookDTO = ProjectHookDTO.allHook();
        projectHookDTO.setEnableSslVerification(true);
        projectHookDTO.setProjectId(gitlabProjectDO.getId());
        projectHookDTO.setToken(devopsEnvironmentDTO.getToken());
        String uri = !gatewayUrl.endsWith("/") ? gatewayUrl + "/" : gatewayUrl;
        uri += "devops/webhook/git_ops";
        projectHookDTO.setUrl(uri);
        List<ProjectHookDTO> projectHookDTOS = gitlabServiceClientOperator.listProjectHook(gitlabProjectDO.getId(),
                gitlabProjectPayload.getUserId());
        if (projectHookDTOS == null || projectHookDTOS.isEmpty()) {
            devopsEnvironmentDTO.setHookId(TypeUtil.objToLong(gitlabServiceClientOperator.createWebHook(
                    gitlabProjectDO.getId(), gitlabProjectPayload.getUserId(), projectHookDTO).getId()));
        } else {
            devopsEnvironmentDTO.setHookId(TypeUtil.objToLong(projectHookDTOS.get(0).getId()));
        }
        if (!gitlabServiceClientOperator.getFile(gitlabProjectDO.getId(), MASTER, README)) {
            gitlabServiceClientOperator.createFile(gitlabProjectDO.getId(),
                    README, README_CONTENT, "ADD README", gitlabProjectPayload.getUserId());
        }

        // 创建环境时初始化用户权限，分为gitlab权限和devops环境用户表权限
        gitlabProjectPayload.setGitlabProjectId(gitlabProjectDO.getId());
        initUserPermissionWhenCreatingEnv(gitlabProjectPayload, devopsEnvironmentDTO.getId(), projectDTO.getId());

        devopsEnvironmentDTO.setSynchro(true);
        baseUpdate(devopsEnvironmentDTO);
    }

    private void initUserPermissionWhenCreatingEnv(EnvGitlabProjectPayload gitlabProjectPayload, Long envId, Long projectId) {

        // 跳过权限检查，项目下所有成员自动分配权限
        if (Boolean.TRUE.equals(gitlabProjectPayload.getSkipCheckPermission())) {
            List<Long> iamUserIds = iamService.getAllMemberIdsWithoutOwner(gitlabProjectPayload.getIamProjectId());
            userAttrService.baseListByUserIds(iamUserIds)
                    .stream()
                    .map(UserAttrDTO::getGitlabUserId)
                    .map(TypeUtil::objToInteger)
                    .forEach(userId -> updateGitlabMemberPermission(
                            gitlabProjectPayload.getGroupId(),
                            gitlabProjectPayload.getGitlabProjectId(),
                            userId)
                    );
            return;
        }

        // 需要分配权限的用户id
        List<Long> userIds = gitlabProjectPayload.getUserIds();
        if (userIds == null || userIds.isEmpty()) {
            return;
        }

        // 获取项目下所有项目成员
        PageInfo<UserVO> allProjectMemberPage = getMembersFromProject(new PageRequest(0, 0), projectId, "");

        // 所有项目成员中有权限的
        allProjectMemberPage.getList().stream().filter(e -> userIds.contains(e.getId())).forEach(e -> {
            Long userId = e.getId();
            String loginName = e.getLoginName();
            String realName = e.getRealName();
            UserAttrDTO userAttrDTO = userAttrService.baseQueryById(userId);

            updateGitlabMemberPermission(
                    gitlabProjectPayload.getGroupId(),
                    gitlabProjectPayload.getGitlabProjectId(),
                    userAttrDTO.getGitlabUserId().intValue());
            // 添加devops数据库记录
            devopsEnvUserPermissionService.baseCreate(new DevopsEnvUserPermissionDTO(loginName, userId, realName, envId, true));
        });
    }

    /**
     * update gitlab project member permission
     *
     * @param gitlabGroupId   gitlab group id
     * @param gitlabProjectId gitlab project id
     * @param gitlabUserId    gitlab user id
     */
    private void updateGitlabMemberPermission(Integer gitlabGroupId, Integer gitlabProjectId, Integer gitlabUserId) {
        // 删除组和用户之间的关系，如果存在
        MemberDTO memberDTO = gitlabGroupMemberService.queryByUserId(gitlabGroupId, TypeUtil.objToInteger(gitlabUserId));
        if (memberDTO != null) {
            gitlabGroupMemberService.delete(gitlabGroupId, TypeUtil.objToInteger(gitlabUserId));
        }
        // 当项目不存在用户权限纪录时(防止失败重试时报成员已存在异常)，添加gitlab用户权限
        MemberDTO gitlabMemberDTO = gitlabServiceClientOperator.getProjectMember(gitlabProjectId, TypeUtil.objToInteger(gitlabUserId));
        if (gitlabMemberDTO == null || gitlabMemberDTO.getUserId() == null) {
            gitlabServiceClientOperator.createProjectMember(gitlabProjectId, new MemberDTO(TypeUtil.objToInteger(gitlabUserId), 40, ""));
        }
    }

    @Override
    public EnvSyncStatusVO queryEnvSyncStatus(Long projectId, Long envId) {
        ProjectDTO projectDTO = iamService.queryIamProject(projectId);
        OrganizationDTO organizationDTO = iamService.queryOrganizationById(projectDTO.getOrganizationId());
        DevopsEnvironmentDTO devopsEnvironmentDTO = baseQueryById(envId);
        if (devopsEnvironmentDTO == null) {
            return null;
        }
        EnvSyncStatusVO envSyncStatusDTO = new EnvSyncStatusVO();
        if (devopsEnvironmentDTO.getAgentSyncCommit() != null) {
            envSyncStatusDTO.setAgentSyncCommit(devopsEnvCommitService
                    .baseQuery(devopsEnvironmentDTO.getAgentSyncCommit()).getCommitSha());
        }
        if (devopsEnvironmentDTO.getDevopsSyncCommit() != null) {
            envSyncStatusDTO.setDevopsSyncCommit(devopsEnvCommitService
                    .baseQuery(devopsEnvironmentDTO.getDevopsSyncCommit())
                    .getCommitSha());
        }
        if (devopsEnvironmentDTO.getSagaSyncCommit() != null) {
            envSyncStatusDTO.setSagaSyncCommit(devopsEnvCommitService
                    .baseQuery(devopsEnvironmentDTO.getSagaSyncCommit()).getCommitSha());
        }

        gitlabUrl = gitlabUrl.endsWith("/") ? gitlabUrl.substring(0, gitlabUrl.length() - 1) : gitlabUrl;
        envSyncStatusDTO.setCommitUrl(String.format("%s/%s-%s-gitops/%s/commit/",
                gitlabUrl, organizationDTO.getCode(), projectDTO.getCode(),
                devopsEnvironmentDTO.getCode()));
        return envSyncStatusDTO;
    }

    @Override
    public PageInfo<DevopsEnvUserVO> listUserPermissionByEnvId(Long projectId, PageRequest pageRequest,
                                                               String searchParams, Long envId) {
        if (envId == null) {
            // 根据项目成员id查询项目下所有的项目成员
            PageInfo<UserVO> allProjectMemberPage = getMembersFromProject(pageRequest, projectId, searchParams);

            PageInfo<DevopsEnvUserVO> devopsEnvUserPermissionDTOPage = new PageInfo<>();
            List<DevopsEnvUserVO> allProjectMemberList = allProjectMemberPage.getList()
                    .stream()
                    .map(iamUser -> new DevopsEnvUserVO(iamUser.getId(), iamUser.getLoginName(), iamUser.getRealName()))
                    .collect(Collectors.toList());
            BeanUtils.copyProperties(allProjectMemberPage, devopsEnvUserPermissionDTOPage);
            devopsEnvUserPermissionDTOPage.setList(allProjectMemberList);
            return devopsEnvUserPermissionDTOPage;
        } else {
            // 普通分页需要带上iam中的所有项目成员，如果iam中的项目所有者也带有项目成员的身份，则需要去掉
            PageInfo<UserVO> allProjectMemberPage = getMembersFromProject(pageRequest, projectId, searchParams);
            List<DevopsEnvUserVO> retureUsersDTOList = allProjectMemberPage.getList()
                    .stream()
                    .map(iamUser -> new DevopsEnvUserVO(iamUser.getId(), iamUser.getLoginName(), iamUser.getRealName()))
                    .collect(Collectors.toList());
            PageInfo<DevopsEnvUserVO> devopsEnvUserPermissionDTOPage = new PageInfo<>();
            BeanUtils.copyProperties(allProjectMemberPage, devopsEnvUserPermissionDTOPage);
            devopsEnvUserPermissionDTOPage.setList(retureUsersDTOList);
            return devopsEnvUserPermissionDTOPage;
        }
    }

    @Override
    public PageInfo<DevopsUserPermissionVO> pageUserPermissionByEnvId(Long projectId, PageRequest pageRequest, String params, Long envId) {
        DevopsEnvironmentDTO devopsEnvironmentDTO = devopsEnvironmentMapper.selectByPrimaryKey(envId);

        RoleAssignmentSearchVO roleAssignmentSearchVO = new RoleAssignmentSearchVO();
        Map<String, Object> searchParamMap = null;
        String param = null;
        // 处理搜索参数
        if (!StringUtils.isEmpty(params)) {
            Map maps = gson.fromJson(params, Map.class);
            searchParamMap = TypeUtil.cast(maps.get(TypeUtil.SEARCH_PARAM));
            param = TypeUtil.cast(maps.get(TypeUtil.PARAM));
            roleAssignmentSearchVO.setParam(new String[]{param});
            if (searchParamMap != null) {
                if (searchParamMap.get("loginName") != null) {
                    String loginName = TypeUtil.objToString(searchParamMap.get("loginName"));
                    roleAssignmentSearchVO.setLoginName(loginName);
                }
                if (searchParamMap.get("realName") != null) {
                    String realName = TypeUtil.objToString(searchParamMap.get("realName"));
                    roleAssignmentSearchVO.setRealName(realName);
                }
            }
        }

        // 根据搜索参数查询所有的项目所有者
        Long ownerId = iamService.queryRoleIdByCode(PROJECT_OWNER);
        PageInfo<IamUserDTO> projectOwners = iamService.pagingQueryUsersByRoleIdOnProjectLevel(
                new PageRequest(0, 0), roleAssignmentSearchVO, ownerId, projectId, false);

        List<DevopsUserPermissionVO> members;
        if(!devopsEnvironmentDTO.getSkipCheckPermission()) {
            // 根据搜索参数查询数据库中所有的环境权限分配数据
            List<DevopsEnvUserPermissionDTO> permissions = devopsEnvUserPermissionMapper.listUserEnvPermissionByOption(envId, searchParamMap, param);
            members = permissions
                    .stream()
                    .map(p -> ConvertUtils.convertObject(p, DevopsUserPermissionVO.class))
                    .peek(p -> p.setRole(MEMBER))
                    .collect(Collectors.toList());
        } else {
            // 搜索所有的项目成员，并过滤其中的项目所有者
            Long memberRoleId = iamService.queryRoleIdByCode(PROJECT_MEMBER);
            List<Long> ownerIds = projectOwners
                    .getList()
                    .stream()
                    .map(IamUserDTO::getId)
                    .collect(Collectors.toList());
            members = iamService.pagingQueryUsersByRoleIdOnProjectLevel(
                    new PageRequest(0, 0), roleAssignmentSearchVO, memberRoleId, projectId, false)
                    .getList()
                    .stream()
                    .filter(u -> !ownerIds.contains(u.getId()))
                    .map(iamUser -> new DevopsUserPermissionVO(iamUser.getId(), iamUser.getLoginName(), iamUser.getRealName()))
                    .peek(p -> p.setRole(MEMBER))
                    .collect(Collectors.toList());
        }

        // 项目成员加上项目所有者
        List<DevopsUserPermissionVO> owners = projectOwners.getList()
                .stream()
                .map(iamUser -> new DevopsUserPermissionVO(iamUser.getId(), iamUser.getLoginName(), iamUser.getRealName()))
                .peek(p -> p.setRole(OWNER))
                .collect(Collectors.toList());
        members.addAll(owners);

        // 根据结果手动设置page的相关属性
        return PageInfoUtil.createPageFromList(members, pageRequest);
    }

    @Override
    public List<DevopsEnvUserVO> listNonRelatedMembers(Long projectId, Long envId, String params) {
        RoleAssignmentSearchVO roleAssignmentSearchVO = new RoleAssignmentSearchVO();
        // 处理搜索参数
        if (!StringUtils.isEmpty(params)) {
            Map maps = gson.fromJson(params, Map.class);
            Map<String, Object> searchParamMap = TypeUtil.cast(maps.get(TypeUtil.SEARCH_PARAM));
            String param = TypeUtil.cast(maps.get(TypeUtil.PARAM));

            roleAssignmentSearchVO.setParam(new String[]{param});
            if (searchParamMap.get("loginName") != null) {
                String loginName = TypeUtil.objToString(searchParamMap.get("loginName"));
                roleAssignmentSearchVO.setLoginName(loginName);
            }

            if (searchParamMap.get("realName") != null) {
                String realName = TypeUtil.objToString(searchParamMap.get("realName"));
                roleAssignmentSearchVO.setRealName(realName);
            }
        }

        // 根据参数搜索所有的项目成员
        Long memberRoleId = iamService.queryRoleIdByCode(PROJECT_MEMBER);
        PageInfo<IamUserDTO> allProjectMembers = iamService.pagingQueryUsersByRoleIdOnProjectLevel(
                new PageRequest(0, 0), roleAssignmentSearchVO, memberRoleId, projectId, false);
        if (allProjectMembers.getList().isEmpty()) {
            return Collections.emptyList();
        }

        // 获取项目下所有的项目所有者（带上搜索参数搜索可以获得更精确的结果）
        Long ownerId = iamService.queryRoleIdByCode(PROJECT_OWNER);
        List<Long> allProjectOwnerIds = iamService.pagingQueryUsersByRoleIdOnProjectLevel(
                new PageRequest(0, 0), roleAssignmentSearchVO, ownerId, projectId, false)
                .getList()
                .stream()
                .map(IamUserDTO::getId)
                .collect(Collectors.toList());

        // 数据库中已被分配权限的
        List<Long> assigned = devopsEnvUserPermissionMapper.listUserIdsByEnvId(envId);

        // 过滤项目成员中的项目所有者和已被分配权限的
        List<IamUserDTO> members = allProjectMembers.getList()
                .stream()
                .filter(member -> !allProjectOwnerIds.contains(member.getId()))
                .filter(member -> !assigned.contains(member.getId()))
                .collect(Collectors.toList());

        return ConvertUtils.convertList(members,
                iamUserDTO -> new DevopsEnvUserVO(iamUserDTO.getId(), iamUserDTO.getLoginName(), iamUserDTO.getRealName()));
    }

    @Override
    public void deletePermissionOfUser(Long envId, Long userId) {
        DevopsEnvUserPermissionDTO devopsEnvUserPermissionDTO = new DevopsEnvUserPermissionDTO();
        devopsEnvUserPermissionDTO.setEnvId(envId);
        devopsEnvUserPermissionDTO.setIamUserId(userId);
        devopsEnvUserPermissionMapper.delete(devopsEnvUserPermissionDTO);
    }

    @Override
    public List<DevopsEnvUserVO> listAllUserPermission(Long envId) {
        return ConvertUtils.convertList(devopsEnvUserPermissionService.baseListByEnvId(envId), DevopsEnvUserVO.class);
    }


    @Saga(code = SagaTopicCodeConstants.DEVOPS_UPDATE_ENV_PERMISSION, description = "更新环境的权限")
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean updateEnvUserPermission(DevopsEnvPermissionUpdateVO devopsEnvPermissionUpdateVO) {
        DevopsEnvironmentDTO preEnvironmentDTO = devopsEnvironmentMapper.selectByPrimaryKey(devopsEnvPermissionUpdateVO.getEnvId());

        DevopsEnvUserPayload userPayload = new DevopsEnvUserPayload();
        userPayload.setEnvId(preEnvironmentDTO.getId());
        userPayload.setGitlabProjectId(preEnvironmentDTO.getGitlabEnvProjectId().intValue());
        userPayload.setIamProjectId(preEnvironmentDTO.getProjectId());
        userPayload.setIamUserIds(devopsEnvPermissionUpdateVO.getUserIds());

        // 判断更新的情况
        if (preEnvironmentDTO.getSkipCheckPermission()) {
            if (devopsEnvPermissionUpdateVO.getSkipCheckPermission()) {
                return Boolean.FALSE;
            } else {
                // 添加权限
                List<IamUserDTO> addIamUsers = iamService.listUsersByIds(devopsEnvPermissionUpdateVO.getUserIds());
                addIamUsers.forEach(e -> devopsEnvUserPermissionService.baseCreate(new DevopsEnvUserPermissionDTO(e.getLoginName(), e.getId(), e.getRealName(), preEnvironmentDTO.getId(), true)));

                userPayload.setOption(1);

                // 更新字段
                preEnvironmentDTO.setSkipCheckPermission(devopsEnvPermissionUpdateVO.getSkipCheckPermission());
                preEnvironmentDTO.setObjectVersionNumber(devopsEnvPermissionUpdateVO.getObjectVersionNumber());
                devopsEnvironmentMapper.updateByPrimaryKeySelective(preEnvironmentDTO);
            }
        } else {
            if (devopsEnvPermissionUpdateVO.getSkipCheckPermission()) {
                // 删除原先所有的分配情况
                devopsEnvUserPermissionService.deleteByEnvId(preEnvironmentDTO.getId());
                userPayload.setOption(2);

                // 更新字段
                preEnvironmentDTO.setSkipCheckPermission(devopsEnvPermissionUpdateVO.getSkipCheckPermission());
                preEnvironmentDTO.setObjectVersionNumber(devopsEnvPermissionUpdateVO.getObjectVersionNumber());
                devopsEnvironmentMapper.updateByPrimaryKeySelective(preEnvironmentDTO);
            } else {
                // 待添加的用户
                List<Long> addIamUserIds = devopsEnvPermissionUpdateVO.getUserIds();

                List<Integer> addGitlabUserIds = userAttrService.baseListByUserIds(addIamUserIds)
                        .stream()
                        .map(UserAttrDTO::getGitlabUserId)
                        .map(TypeUtil::objToInteger)
                        .collect(Collectors.toList());
                userPayload.setAddGitlabUserIds(addGitlabUserIds);
                userPayload.setDeleteGitlabUserIds(Collections.emptyList());

                devopsEnvUserPermissionService.baseUpdate(devopsEnvPermissionUpdateVO.getEnvId(), addIamUserIds, Collections.emptyList());

                userPayload.setOption(3);
            }
        }

        // 发送saga进行相应的gitlab侧的数据处理
        producer.apply(
                StartSagaBuilder.newBuilder()
                        .withSourceId(preEnvironmentDTO.getProjectId())
                        .withLevel(ResourceLevel.PROJECT)
                        .withPayloadAndSerialize(userPayload)
                        .withSagaCode(SagaTopicCodeConstants.DEVOPS_UPDATE_ENV_PERMISSION),
                builder -> {
                });
        return true;
    }


    private PageInfo<UserVO> getMembersFromProject(PageRequest pageRequest, Long projectId, String searchParams) {
        RoleAssignmentSearchVO roleAssignmentSearchVO = new RoleAssignmentSearchVO();
        if (!StringUtils.isEmpty(searchParams)) {
            Map maps = gson.fromJson(searchParams, Map.class);
            Map<String, Object> searchParamMap = TypeUtil.cast(maps.get(TypeUtil.SEARCH_PARAM));
            String param = TypeUtil.cast(maps.get(TypeUtil.PARAM));
            roleAssignmentSearchVO.setParam(new String[]{param});
            if (searchParamMap.get("loginName") != null) {
                String loginName = TypeUtil.objToString(searchParamMap.get("loginName"));
                String subLogin = loginName.substring(1, loginName.length() - 1);
                roleAssignmentSearchVO.setLoginName(subLogin);
            }
            if (searchParamMap.get("realName") != null) {
                String realName = TypeUtil.objToString(searchParamMap.get("realName"));
                String subReal = realName.substring(1, realName.length() - 1);
                roleAssignmentSearchVO.setRealName(subReal);
            }
        }
        // 获取项目所有者角色id和数量
        Long ownerId = iamService.queryRoleIdByCode(PROJECT_OWNER);
        // 获取项目成员id
        Long memberId = iamService.queryRoleIdByCode(PROJECT_MEMBER);
        // 所有项目成员，可能还带有项目所有者的角色
        PageInfo<IamUserDTO> allMemberWithOtherUsersPage = iamService
                .pagingQueryUsersByRoleIdOnProjectLevel(new PageRequest(0, 0), roleAssignmentSearchVO,
                        memberId, projectId, false);
        // 所有项目所有者
        PageInfo<IamUserDTO> allOwnerUsersPage = iamService
                .pagingQueryUsersByRoleIdOnProjectLevel(new PageRequest(0, 0), roleAssignmentSearchVO,
                        ownerId, projectId, false);
        //合并项目所有者和项目成员
        Set<IamUserDTO> iamUserDTOS = new HashSet<>(allMemberWithOtherUsersPage.getList());
        iamUserDTOS.addAll(allOwnerUsersPage.getList());
        List<IamUserDTO> returnUserDTOList = null;

        //没有任何项目成员和项目所有者
        if (iamUserDTOS.isEmpty()) {
            return ConvertUtils.convertPage(allMemberWithOtherUsersPage, UserVO.class);
        } else {
            returnUserDTOList = iamUserDTOS.stream()
                    .peek(e -> {
                        if (!allOwnerUsersPage.getList().contains(e)) {
                            e.setProjectOwner(false);
                        } else {
                            e.setProjectOwner(true);
                        }
                    }).collect(Collectors.toList());
        }
        allMemberWithOtherUsersPage.setPageSize(pageRequest.getSize());
        allMemberWithOtherUsersPage.setTotal(returnUserDTOList.size());
        allMemberWithOtherUsersPage.setPageNum(pageRequest.getPage());
        if (returnUserDTOList.size() < pageRequest.getSize() * pageRequest.getPage()) {
            allMemberWithOtherUsersPage.setSize(TypeUtil.objToInt(returnUserDTOList.size()) - (pageRequest.getSize() * (pageRequest.getPage() - 1)));
            allMemberWithOtherUsersPage.setList(returnUserDTOList);
        } else {
            allMemberWithOtherUsersPage.setSize(pageRequest.getSize());
            int fromIndex = pageRequest.getSize() * (pageRequest.getPage() - 1);
            int toIndex = (pageRequest.getSize() * pageRequest.getPage()) > returnUserDTOList.size() ? returnUserDTOList.size() : pageRequest.getSize() * pageRequest.getPage();
            allMemberWithOtherUsersPage.setList(returnUserDTOList.subList(fromIndex, toIndex));
        }

        return ConvertUtils.convertPage(allMemberWithOtherUsersPage, UserVO.class);
    }

    private void setPermission(DevopsEnvironmentDTO devopsEnvironmentDTO, List<Long> permissionEnvIds,
                               Boolean isProjectOwner) {
        if (permissionEnvIds.contains(devopsEnvironmentDTO.getId()) || isProjectOwner) {
            devopsEnvironmentDTO.setPermission(true);
        } else {
            devopsEnvironmentDTO.setPermission(false);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteDeactivatedEnvironment(Long envId) {
        DevopsEnvironmentDTO devopsEnvironmentDTO = baseQueryById(envId);
        // 删除环境对应的实例
        appServiceInstanceService.baseListByEnvId(envId).forEach(instanceE ->
                devopsEnvCommandService.baseListByObject(HelmObjectKind.INSTANCE.toValue(), instanceE.getId()).forEach(t -> devopsEnvCommandService.baseDeleteByEnvCommandId(t)));
        appServiceInstanceService.deleteByEnvId(envId);
        // 删除环境对应的域名、域名路径
        devopsIngressService.baseListByEnvId(envId).forEach(ingressE ->
                devopsEnvCommandService.baseListByObject(HelmObjectKind.INGRESS.toValue(), ingressE.getId()).forEach(t -> devopsEnvCommandService.baseDeleteByEnvCommandId(t)));
        devopsIngressService.deleteIngressAndIngressPathByEnvId(envId);
        // 删除环境对应的网络和网络实例
        devopsServiceService.baseListByEnvId(envId).forEach(serviceE ->
                devopsEnvCommandService.baseListByObject(HelmObjectKind.SERVICE.toValue(), serviceE.getId()).forEach(t -> devopsEnvCommandService.baseDeleteByEnvCommandId(t)));
        devopsServiceService.baseDeleteServiceAndInstanceByEnvId(envId);

        // 删除环境
        baseDeleteById(envId);
        // 删除gitlab库, 删除之前查询是否存在
        if (devopsEnvironmentDTO.getGitlabEnvProjectId() != null) {
            Integer gitlabProjectId = TypeUtil.objToInt(devopsEnvironmentDTO.getGitlabEnvProjectId());
            GitlabProjectDTO gitlabProjectDO = gitlabServiceClientOperator.queryProjectById(gitlabProjectId);
            if (gitlabProjectDO != null && gitlabProjectDO.getId() != null) {
                UserAttrDTO userAttrDTO = userAttrService.baseQueryById(TypeUtil.objToLong(GitUserNameUtil.getUserId()));
                Integer gitlabUserId = TypeUtil.objToInt(userAttrDTO.getGitlabUserId());
                gitlabServiceClientOperator.deleteProjectById(gitlabProjectId, gitlabUserId);
            }
        }
        // 删除环境命名空间
        if (devopsEnvironmentDTO.getClusterId() != null) {
            agentCommandService.deleteEnv(envId, devopsEnvironmentDTO.getCode(), devopsEnvironmentDTO.getClusterId());
        }
    }

    @Override
    public List<DevopsClusterRepVO> listDevopsCluster(Long projectId) {
        ProjectDTO projectDTO = iamService.queryIamProject(projectId);
        List<DevopsClusterRepVO> devopsClusterRepVOS = ConvertUtils.convertList(devopsClusterService.baseListByProjectId(projectId, projectDTO.getOrganizationId()), DevopsClusterRepVO.class);
        List<Long> connectedClusterList = clusterConnectionHandler.getConnectedEnvList();
        List<Long> upgradeClusterList = clusterConnectionHandler.getUpdatedEnvList();
        devopsClusterRepVOS.forEach(t -> {
            if (connectedClusterList.contains(t.getId()) && upgradeClusterList.contains(t.getId())) {
                t.setConnect(true);
            } else {
                t.setConnect(false);
            }
        });
        return devopsClusterRepVOS;
    }

    @Override
    @Saga(code = SagaTopicCodeConstants.DEVOPS_SET_ENV_ERR,
            description = "devops创建环境失败(devops set env status create err)", inputSchema = "{}")
    public void setEnvErrStatus(String data, Long projectId) {
        //todo
        //data转未完成
        producer.applyAndReturn(
                StartSagaBuilder
                        .newBuilder()
                        .withLevel(ResourceLevel.PROJECT)
                        .withRefType("")
                        .withSagaCode(SagaTopicCodeConstants.DEVOPS_SET_ENV_ERR),
                builder -> builder
                        .withPayloadAndSerialize(data)
                        .withRefId("")
                        .withSourceId(projectId));
    }

    @Override
    public DevopsEnviromentRepVO queryByCode(Long clusterId, String code) {
        return ConvertUtils.convertObject(baseQueryByProjectIdAndCode(clusterId, code), DevopsEnviromentRepVO.class);
    }


    @Override
    public DevopsEnvironmentDTO baseCreate(DevopsEnvironmentDTO devopsEnvironmentDTO) {
        if (devopsEnvironmentMapper.insert(devopsEnvironmentDTO) != 1) {
            throw new CommonException("error.environment.create");
        }
        return devopsEnvironmentDTO;
    }

    @Override
    public DevopsEnvironmentDTO baseQueryById(Long id) {
        return devopsEnvironmentMapper.selectByPrimaryKey(id);
    }

    @Override
    public Boolean baseUpdateActive(Long environmentId, Boolean active) {
        DevopsEnvironmentDTO devopsEnvironmentDTO = devopsEnvironmentMapper.selectByPrimaryKey(environmentId);
        devopsEnvironmentDTO.setId(environmentId);
        devopsEnvironmentDTO.setActive(active);
        devopsEnvironmentDTO.setObjectVersionNumber(devopsEnvironmentDTO.getObjectVersionNumber());
        if (devopsEnvironmentMapper.updateByPrimaryKeySelective(devopsEnvironmentDTO) != 1) {
            throw new CommonException("error.environment.update");
        }
        return true;
    }

    @Override
    public DevopsEnvironmentDTO baseUpdate(DevopsEnvironmentDTO devopsEnvironmentDTO) {
        devopsEnvironmentDTO.setObjectVersionNumber(devopsEnvironmentMapper.selectByPrimaryKey(
                devopsEnvironmentDTO.getId()).getObjectVersionNumber());
        if (devopsEnvironmentDTO.getDevopsEnvGroupId() == null) {
            devopsEnvironmentMapper.updateDevopsEnvGroupId(devopsEnvironmentDTO.getId());
        }
        if (devopsEnvironmentMapper.updateByPrimaryKeySelective(devopsEnvironmentDTO) != 1) {
            throw new CommonException("error.environment.update");
        }
        return devopsEnvironmentDTO;
    }

    @Override
    public void baseCheckCode(DevopsEnvironmentDTO devopsEnvironmentDTO) {
        DevopsEnvironmentDTO environmentDTO = new DevopsEnvironmentDTO();
        environmentDTO.setClusterId(devopsEnvironmentDTO.getClusterId());
        environmentDTO.setCode(devopsEnvironmentDTO.getCode());
        if (!devopsEnvironmentMapper.select(environmentDTO).isEmpty()) {
            throw new CommonException("error.code.exist");
        }
        environmentDTO.setClusterId(null);
        environmentDTO.setProjectId(devopsEnvironmentDTO.getProjectId());
        if (!devopsEnvironmentMapper.select(environmentDTO).isEmpty()) {
            throw new CommonException("error.code.exist");
        }
    }

    @Override
    public List<DevopsEnvironmentDTO> baseListByProjectId(Long projectId) {
        DevopsEnvironmentDTO environmentDTO = new DevopsEnvironmentDTO();
        environmentDTO.setProjectId(projectId);
        return devopsEnvironmentMapper.select(environmentDTO);
    }

    @Override
    public List<DevopsEnvironmentDTO> baseListByProjectIdAndActive(Long projectId, Boolean active) {
        DevopsEnvironmentDTO devopsEnvironmentDTO = new DevopsEnvironmentDTO();
        devopsEnvironmentDTO.setProjectId(projectId);
        devopsEnvironmentDTO.setActive(active);
        return devopsEnvironmentMapper.select(devopsEnvironmentDTO);
    }

    @Override
    public DevopsEnvironmentDTO baseQueryByClusterIdAndCode(Long clusterId, String code) {
        DevopsEnvironmentDTO devopsEnvironmentDTO = new DevopsEnvironmentDTO();
        devopsEnvironmentDTO.setClusterId(clusterId);
        devopsEnvironmentDTO.setCode(code);
        return devopsEnvironmentMapper.selectOne(devopsEnvironmentDTO);
    }

    @Override
    public DevopsEnvironmentDTO baseQueryByProjectIdAndCode(Long projectId, String code) {
        DevopsEnvironmentDTO devopsEnvironmentDTO = new DevopsEnvironmentDTO();
        devopsEnvironmentDTO.setProjectId(projectId);
        devopsEnvironmentDTO.setCode(code);
        return devopsEnvironmentMapper.selectOne(devopsEnvironmentDTO);
    }

    @Override
    public DevopsEnvironmentDTO baseQueryByToken(String token) {
        return devopsEnvironmentMapper.queryByToken(token);
    }

    @Override
    public List<DevopsEnvironmentDTO> baseListAll() {
        return devopsEnvironmentMapper.selectAll();
    }

    @Override
    public void baseUpdateSagaSyncEnvCommit(DevopsEnvironmentDTO devopsEnvironmentDTO) {
        devopsEnvironmentMapper.updateSagaSyncEnvCommit(devopsEnvironmentDTO.getId(),
                devopsEnvironmentDTO.getSagaSyncCommit());
    }

    @Override
    public void baseUpdateDevopsSyncEnvCommit(DevopsEnvironmentDTO devopsEnvironmentDTO) {
        devopsEnvironmentMapper.updateDevopsSyncEnvCommit(devopsEnvironmentDTO.getId(),
                devopsEnvironmentDTO.getDevopsSyncCommit());
    }

    @Override
    public void baseUpdateAgentSyncEnvCommit(DevopsEnvironmentDTO devopsEnvironmentDTO) {
        devopsEnvironmentMapper.updateAgentSyncEnvCommit(devopsEnvironmentDTO.getId(),
                devopsEnvironmentDTO.getAgentSyncCommit());
    }


    @Override
    public void baseDeleteById(Long id) {
        devopsEnvironmentMapper.deleteByPrimaryKey(id);
    }

    @Override
    public List<DevopsEnvironmentDTO> baseListByClusterId(Long clusterId) {
        DevopsEnvironmentDTO devopsEnvironmentDO = new DevopsEnvironmentDTO();
        devopsEnvironmentDO.setClusterId(clusterId);
        return devopsEnvironmentMapper.select(devopsEnvironmentDO);
    }

    @Override
    public List<DevopsEnvironmentDTO> baseListByIds(List<Long> envIds) {
        return devopsEnvironmentMapper.listByIds(envIds);
    }

}
