package io.choerodon.devops.app.service.impl;

import java.util.*;
import java.util.stream.Collectors;

import com.alibaba.fastjson.JSONArray;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.pagehelper.PageInfo;
import com.google.gson.Gson;
import io.choerodon.asgard.saga.annotation.Saga;
import io.choerodon.asgard.saga.dto.StartInstanceDTO;
import io.choerodon.asgard.saga.feign.SagaClient;
import io.choerodon.base.domain.PageRequest;
import io.choerodon.core.convertor.ConvertHelper;
import io.choerodon.core.exception.CommonException;
import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.devops.api.validator.DevopsEnvironmentValidator;
import io.choerodon.devops.api.vo.*;
import io.choerodon.devops.api.vo.iam.UserVO;
import io.choerodon.devops.app.eventhandler.payload.GitlabProjectPayload;
import io.choerodon.devops.app.service.AgentCommandService;
import io.choerodon.devops.app.service.DevopsEnvironmentService;
import io.choerodon.devops.app.service.DevopsGitService;
import io.choerodon.devops.app.service.GitlabGroupMemberService;
import io.choerodon.devops.domain.application.repository.*;
import io.choerodon.devops.domain.application.valueobject.OrganizationVO;
import io.choerodon.devops.infra.common.util.enums.EnvironmentGitopsStatus;
import io.choerodon.devops.infra.dataobject.DevopsEnvironmentInfoDTO;
import io.choerodon.devops.infra.dataobject.gitlab.CommitDTO;
import io.choerodon.devops.infra.dataobject.gitlab.GitlabProjectDTO;
import io.choerodon.devops.infra.dto.DevopsEnvironmentDTO;
import io.choerodon.devops.infra.dto.gitlab.ProjectHookDTO;
import io.choerodon.devops.infra.enums.AccessLevel;
import io.choerodon.devops.infra.enums.HelmObjectKind;
import io.choerodon.devops.infra.enums.InstanceStatus;
import io.choerodon.devops.infra.handler.ClusterConnectionHandler;
import io.choerodon.devops.infra.mapper.DevopsEnvFileErrorMapper;
import io.choerodon.devops.infra.mapper.DevopsEnvironmentMapper;
import io.choerodon.devops.infra.util.FileUtil;
import io.choerodon.devops.infra.util.GenerateUUID;
import io.choerodon.devops.infra.util.GitUserNameUtil;
import io.choerodon.devops.infra.util.TypeUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Created by younger on 2018/4/9.
 */
@Service
public class DevopsEnvironmentServiceImpl implements DevopsEnvironmentService {

    private static final Gson gson = new Gson();
    private static final String MASTER = "master";
    private static final String README = "README.md";
    private static final String README_CONTENT =
            "# This is gitops env repository!";
    private static final String ENV = "ENV";
    private static final String PROJECT_OWNER = "role/project/default/project-owner";
    private static final String PROJECT_MEMBER = "role/project/default/project-member";
    private ObjectMapper objectMapper = new ObjectMapper();

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
    private IamRepository iamRepository;
    @Autowired
    private DevopsEnvironmentRepository devopsEnviromentRepository;
    @Autowired
    private DevopsEnvUserPermissionRepository devopsEnvUserPermissionRepository;
    @Autowired
    private DevopsServiceRepository devopsServiceRepository;
    @Autowired
    private ApplicationInstanceRepository applicationInstanceRepository;
    @Autowired
    private DevopsEnvironmentValidator devopsEnvironmentValidator;
    @Autowired
    private ClusterConnectionHandler clusterConnectionHandler;
    @Autowired
    private SagaClient sagaClient;
    @Autowired
    private DevopsProjectRepository devopsProjectRepository;
    @Autowired
    private UserAttrRepository userAttrRepository;
    @Autowired
    private GitlabRepository gitlabRepository;
    @Autowired
    private DevopsEnvCommitRepository devopsEnvCommitRepository;
    @Autowired
    private DevopsEnvGroupRepository devopsEnvGroupRepository;
    @Autowired
    private DevopsIngressRepository devopsIngressRepository;
    @Autowired
    private DevopsClusterRepository devopsClusterRepository;
    @Autowired
    private AgentCommandService agentCommandService;
    @Autowired
    private GitlabProjectRepository gitlabProjectRepository;
    @Autowired
    private DevopsEnvCommandRepository devopsEnvCommandRepository;
    @Autowired
    private GitlabGroupMemberRepository gitlabGroupMemberRepository;
    @Autowired
    private DevopsGitService devopsGitService;
    @Autowired
    private GitlabGroupMemberService gitlabGroupMemberService;
    @Autowired
    private DevopsEnvironmentMapper environmentMapper;
    @Autowired
    private DevopsEnvFileErrorMapper devopsEnvFileErrorMapper;
    @Autowired
    private DevopsEnvironmentMapper devopsEnvironmentMapper;


    @Override
    @Saga(code = "devops-create-env", description = "创建环境", inputSchema = "{}")
    @Transactional
    public void create(Long projectId, DevopsEnviromentDTO devopsEnviromentDTO) {
        DevopsEnvironmentE devopsEnvironmentE = ConvertHelper.convert(devopsEnviromentDTO, DevopsEnvironmentE.class);
        devopsEnvironmentE.initProjectE(projectId);
        checkCode(projectId, devopsEnviromentDTO.getClusterId(), devopsEnviromentDTO.getCode());
        devopsEnvironmentE.initActive(true);
        devopsEnvironmentE.initConnect(false);
        devopsEnvironmentE.initSynchro(false);
        devopsEnvironmentE.initFailed(false);
        devopsEnvironmentE.initDevopsClusterEById(devopsEnviromentDTO.getClusterId());
        devopsEnvironmentE.initToken(GenerateUUID.generateUUID());
        devopsEnvironmentE.initProjectE(projectId);
        ProjectVO projectE = iamRepository.queryIamProject(projectId);
        OrganizationVO organization = iamRepository.queryOrganizationById(projectE.getOrganization().getId());

        UserAttrE userAttrE = userAttrRepository.baseQueryById(TypeUtil.objToLong(GitUserNameUtil.getUserId()));
        if (userAttrE == null) {
            throw new CommonException("error.gitlab.user.sync.failed");
        }

        // 查询创建应用所在的gitlab应用组
        DevopsProjectVO devopsProjectE = devopsProjectRepository.baseQueryByProjectId(projectId);
        GitlabMemberE gitlabMemberE = gitlabGroupMemberRepository.getUserMemberByUserId(
                TypeUtil.objToInteger(devopsProjectE.getDevopsEnvGroupId()),
                TypeUtil.objToInteger(userAttrE.getGitlabUserId()));
        if (gitlabMemberE == null || gitlabMemberE.getAccessLevel() != AccessLevel.OWNER.toValue()) {
            throw new CommonException("error.user.not.owner");
        }

        List<DevopsEnvironmentE> devopsEnvironmentES = devopsEnviromentRepository
                .baseListByProjectIdAndActive(projectId, true);
        //创建环境没有选环境组，序列从默认组环境递增，创建环境选了环境组，序列从该环境组环境递增
        if (devopsEnviromentDTO.getDevopsEnvGroupId() == null) {
            devopsEnvironmentE.initSequence(devopsEnvironmentES.stream().filter(devopsEnvironmentE1 ->
                    devopsEnvironmentE1.getDevopsEnvGroupId() == null).collect(Collectors.toList()));
        } else {
            devopsEnvironmentE.initSequence(devopsEnvironmentES.stream().filter(devopsEnvironmentE1 ->
                    devopsEnviromentDTO.getDevopsEnvGroupId().equals(devopsEnvironmentE1.getDevopsEnvGroupId()))
                    .collect(Collectors.toList()));
        }
        List<String> sshKeys = FileUtil.getSshKey(
                organization.getCode() + "/" + projectE.getCode() + "/" + devopsEnviromentDTO.getCode());
        devopsEnvironmentE.setEnvIdRsa(sshKeys.get(0));
        devopsEnvironmentE.setEnvIdRsaPub(sshKeys.get(1));
        Long envId = devopsEnviromentRepository.baseCreate(devopsEnvironmentE).getId();
        devopsEnvironmentE.setId(envId);

        GitlabProjectPayload gitlabProjectPayload = new GitlabProjectPayload();
        gitlabProjectPayload.setGroupId(TypeUtil.objToInteger(devopsProjectE.getDevopsEnvGroupId()));
        gitlabProjectPayload.setUserId(TypeUtil.objToInteger(userAttrE.getGitlabUserId()));
        gitlabProjectPayload.setPath(devopsEnviromentDTO.getCode());
        gitlabProjectPayload.setOrganizationId(null);
        gitlabProjectPayload.setType(ENV);
        UserE userE = iamRepository.queryUserByUserId(userAttrE.getIamUserId());
        gitlabProjectPayload.setLoginName(userE.getLoginName());
        gitlabProjectPayload.setRealName(userE.getRealName());
        gitlabProjectPayload.setClusterId(devopsEnviromentDTO.getClusterId());
        gitlabProjectPayload.setIamProjectId(projectId);

        // 创建环境时将项目下所有用户装入payload以便于saga消费
        gitlabProjectPayload.setUserIds(devopsEnviromentDTO.getUserIds());

        String input;
        try {
            input = objectMapper.writeValueAsString(gitlabProjectPayload);
            sagaClient.startSaga("devops-create-env", new StartInstanceDTO(input, "", "", ResourceLevel.PROJECT.value(), projectId));
            agentCommandService.initEnv(devopsEnvironmentE, devopsEnviromentDTO.getClusterId());
        } catch (JsonProcessingException e) {
            throw new CommonException(e.getMessage(), e);
        }
    }

    @Override
    public List<DevopsEnvGroupEnvsDTO> listDevopsEnvGroupEnvs(Long projectId, Boolean active) {
        List<DevopsEnvGroupEnvsDTO> devopsEnvGroupEnvsDTOS = new ArrayList<>();
        List<Long> connectedClusterList = clusterConnectionHandler.getConnectedEnvList();
        List<Long> upgradeClusterList = clusterConnectionHandler.getUpdatedEnvList();
        List<DevopsEnvironmentE> devopsEnvironmentES = devopsEnviromentRepository
                .baseListByProjectIdAndActive(projectId, active).stream().peek(t ->
                        setEnvStatus(connectedClusterList, upgradeClusterList, t)
                )
                .sorted(Comparator.comparing(DevopsEnvironmentE::getSequence))
                .collect(Collectors.toList());
        List<DevopsEnviromentRepDTO> devopsEnviromentRepDTOS = ConvertHelper.convertList(devopsEnvironmentES, DevopsEnviromentRepDTO.class);
        if (!active) {
            DevopsEnvGroupEnvsDTO devopsEnvGroupEnvsDTO = new DevopsEnvGroupEnvsDTO();
            devopsEnvGroupEnvsDTO.setDevopsEnviromentRepDTOs(devopsEnviromentRepDTOS);
            devopsEnvGroupEnvsDTOS.add(devopsEnvGroupEnvsDTO);
            return devopsEnvGroupEnvsDTOS;
        }
        List<DevopsEnvGroupE> devopsEnvGroupES = devopsEnvGroupRepository.baseListByProjectId(projectId);
        devopsEnviromentRepDTOS.forEach(devopsEnviromentRepDTO -> {
            DevopsClusterE devopsClusterE = devopsClusterRepository.baseQuery(devopsEnviromentRepDTO.getClusterId());
            devopsEnviromentRepDTO.setClusterName(devopsClusterE == null ? null : devopsClusterE.getName());
            if (devopsEnviromentRepDTO.getDevopsEnvGroupId() == null) {
                devopsEnviromentRepDTO.setDevopsEnvGroupId(0L);
            }
        });
        //按照环境组分组查询，有环境的环境组放前面，没环境的环境组放后面
        Map<Long, List<DevopsEnviromentRepDTO>> resultMaps = devopsEnviromentRepDTOS.stream()
                .collect(Collectors.groupingBy(DevopsEnviromentRepDTO::getDevopsEnvGroupId));
        List<Long> envGroupIds = new ArrayList<>();
        resultMaps.forEach((key, value) -> {
            envGroupIds.add(key);
            DevopsEnvGroupEnvsDTO devopsEnvGroupEnvsDTO = new DevopsEnvGroupEnvsDTO();
            DevopsEnvGroupE devopsEnvGroupE = new DevopsEnvGroupE();
            if (key != 0) {
                devopsEnvGroupE = devopsEnvGroupRepository.baseQuery(key);
            }
            devopsEnvGroupEnvsDTO.setDevopsEnvGroupId(devopsEnvGroupE.getId());
            devopsEnvGroupEnvsDTO.setDevopsEnvGroupName(devopsEnvGroupE.getName());
            devopsEnvGroupEnvsDTO.setDevopsEnviromentRepDTOs(value);
            devopsEnvGroupEnvsDTOS.add(devopsEnvGroupEnvsDTO);
        });
        devopsEnvGroupES.forEach(devopsEnvGroupE -> {
            if (!envGroupIds.contains(devopsEnvGroupE.getId())) {
                DevopsEnvGroupEnvsDTO devopsEnvGroupEnvsDTO = new DevopsEnvGroupEnvsDTO();
                devopsEnvGroupEnvsDTO.setDevopsEnvGroupId(devopsEnvGroupE.getId());
                devopsEnvGroupEnvsDTO.setDevopsEnvGroupName(devopsEnvGroupE.getName());
                devopsEnvGroupEnvsDTOS.add(devopsEnvGroupEnvsDTO);
            }
        });
        return devopsEnvGroupEnvsDTOS;
    }

    @Override
    public List<DevopsEnviromentRepDTO> listByProjectIdAndActive(Long projectId, Boolean active) {

        // 查询当前用户的环境权限
        List<Long> permissionEnvIds = devopsEnvUserPermissionRepository
                .baseListByUserId(TypeUtil.objToLong(GitUserNameUtil.getUserId())).stream()
                .filter(DevopsEnvUserPermissionE::getPermitted)
                .map(DevopsEnvUserPermissionE::getEnvId).collect(Collectors.toList());
        ProjectVO projectE = iamRepository.queryIamProject(projectId);
        // 查询当前用户是否为项目所有者
        Boolean isProjectOwner = iamRepository
                .isProjectOwner(TypeUtil.objToLong(GitUserNameUtil.getUserId()), projectE);

        List<Long> connectedClusterList = clusterConnectionHandler.getConnectedEnvList();
        List<Long> upgradeClusterList = clusterConnectionHandler.getUpdatedEnvList();
        List<DevopsEnvironmentE> devopsEnvironmentES = devopsEnviromentRepository
                .baseListByProjectIdAndActive(projectId, active).stream()
                .filter(devopsEnvironmentE -> !devopsEnvironmentE.getFailed()).peek(t -> {
                    setEnvStatus(connectedClusterList, upgradeClusterList, t);
                    // 项目成员返回拥有对应权限的环境，项目所有者返回所有环境
                    setPermission(t, permissionEnvIds, isProjectOwner);
                })
                .sorted(Comparator.comparing(DevopsEnvironmentE::getSequence))
                .collect(Collectors.toList());
        return ConvertHelper.convertList(devopsEnvironmentES, DevopsEnviromentRepDTO.class);
    }

    @Override
    public List<DevopsEnviromentRepDTO> listDeployed(Long projectId) {
        List<Long> envList = devopsServiceRepository.baseListEnvByRunningService();
        return listByProjectIdAndActive(projectId, true).stream().filter(t ->
                envList.contains(t.getId())).collect(Collectors.toList());
    }

    @Override
    public List<DevopsEnvironmentViewVO> listEnvTree(Long projectId) {
        List<Long> upgradeClusterList = envUtil.getUpdatedEnvList();

        List<DevopsEnvironmentViewVO> connectedEnvs = new ArrayList<>();
        List<DevopsEnvironmentViewVO> unConnectedEnvs = new ArrayList<>();
        List<DevopsEnvironmentViewVO> unSynchronizedEnvs = new ArrayList<>();

        environmentMapper.listEnvTree(projectId).forEach(e -> {
            // 将DTO层对象转为VO
            DevopsEnvironmentViewVO vo = new DevopsEnvironmentViewVO();
            BeanUtils.copyProperties(e, vo, "apps");
            boolean connected = upgradeClusterList.contains(e.getClusterId());
            vo.setConnect(connected);
            vo.setApps(e.getApps().stream().map(app -> {

                DevopsApplicationViewVO appVO = new DevopsApplicationViewVO();
                BeanUtils.copyProperties(app, appVO, "instances");
                appVO.setInstances(app.getInstances().stream().map(ins -> {
                    DevopsAppInstanceViewVO insVO = new DevopsAppInstanceViewVO();
                    BeanUtils.copyProperties(ins, insVO);
                    return insVO;
                }).collect(Collectors.toList()));

                return appVO;
            }).collect(Collectors.toList()));

            if (connected) {
                connectedEnvs.add(vo);
            } else {
                if (vo.getSynchronize()) {
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
    public Boolean activeEnvironment(Long projectId, Long environmentId, Boolean active) {
        DevopsEnvironmentE devopsEnvironmentE = devopsEnviromentRepository.baseQueryById(environmentId);

        List<Long> upgradeClusterList = clusterConnectionHandler.getUpdatedEnvList();
        List<Long> connectedClusterList = clusterConnectionHandler.getConnectedEnvList();
        setEnvStatus(connectedClusterList, upgradeClusterList, devopsEnvironmentE);
        if (!active && devopsEnvironmentE.getConnect()) {
            devopsEnvironmentValidator.checkEnvCanDisabled(environmentId);
        }
        List<DevopsEnvironmentE> devopsEnvironmentES = devopsEnviromentRepository
                .baseListByProjectIdAndActive(projectId, true);
        devopsEnvironmentE.setActive(active);
        //启用环境，原环境不在环境组内或者原环境所在环境组被删除，则序列在默认组内环境递增，原环境在环境组内，则序列在环境组内环境递增
        if (active) {
            DevopsEnvGroupE devopsEnvGroupE = devopsEnvGroupRepository.baseQuery(devopsEnvironmentE.getDevopsEnvGroupId());
            if (devopsEnvironmentE.getDevopsEnvGroupId() == null || devopsEnvGroupE == null) {
                devopsEnvironmentE.setDevopsEnvGroupId(null);
                devopsEnviromentRepository.baseUpdate(devopsEnvironmentE);
                devopsEnvironmentE.initSequence(devopsEnvironmentES.stream().filter(devopsEnvironmentE1 ->
                        devopsEnvironmentE1.getDevopsEnvGroupId() == null).collect(Collectors.toList()));
            } else {
                devopsEnvironmentE.initSequence(devopsEnvironmentES.stream().filter(devopsEnvironmentE1 ->
                        (devopsEnvironmentE.getDevopsEnvGroupId()).equals(devopsEnvironmentE1.getDevopsEnvGroupId()))
                        .collect(Collectors.toList()));
            }
        } else {
            // 停用环境，环境停用后，原组sequence重新排序
            List<Long> environmentIds;
            if (devopsEnvironmentE.getDevopsEnvGroupId() == null) {
                environmentIds = devopsEnvironmentES.stream()
                        .filter(devopsEnvironmentE1 -> devopsEnvironmentE1.getDevopsEnvGroupId() == null)
                        .sorted(Comparator.comparing(DevopsEnvironmentE::getSequence))
                        .collect(Collectors.toList()).stream()
                        .map(DevopsEnvironmentE::getId)
                        .collect(Collectors.toList());
            } else {
                environmentIds = devopsEnvironmentES.stream()
                        .filter(devopsEnvironmentE1 -> (devopsEnvironmentE.getDevopsEnvGroupId())
                                .equals(devopsEnvironmentE1.getDevopsEnvGroupId()))
                        .sorted(Comparator.comparing(DevopsEnvironmentE::getSequence))
                        .collect(Collectors.toList()).stream()
                        .map(DevopsEnvironmentE::getId)
                        .collect(Collectors.toList());
            }
            environmentIds.remove(environmentId);
            Long[] ids = new Long[environmentIds.size()];
            sort(environmentIds.toArray(ids));
        }
        devopsEnviromentRepository.baseUpdate(devopsEnvironmentE);
        return true;
    }

    @Override
    public DevopsEnvironmentUpdateDTO query(Long environmentId) {
        return ConvertHelper.convert(devopsEnviromentRepository
                .baseQueryById(environmentId), DevopsEnvironmentUpdateDTO.class);
    }

    @Override
    public DevopsEnvironmentInfoVO queryInfoById(Long environmentId) {
        DevopsEnvironmentInfoDTO envInfo = environmentMapper.queryInfoById(environmentId);
        if (envInfo == null) {
            return null;
        }

        DevopsEnvironmentInfoVO vo = new DevopsEnvironmentInfoVO();
        BeanUtils.copyProperties(envInfo, vo);

        List<Long> upgradeClusterList = envUtil.getUpdatedEnvList();
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
    public void checkEnv(DevopsEnvironmentE devopsEnvironmentE, UserAttrE userAttrE) {
        //校验用户是否有环境的权限
        devopsEnvUserPermissionRepository.baseCheckEnvDeployPermission(TypeUtil.objToLong(GitUserNameUtil.getUserId()), devopsEnvironmentE.getId());

        //校验环境是否连接
        clusterConnectionHandler.checkEnvConnection(devopsEnvironmentE.getClusterE().getId());


        //检验gitops库是否存在，校验操作人是否是有gitops库的权限
        gitlabGroupMemberService.checkEnvProject(devopsEnvironmentE, userAttrE);
    }


    @Override
    public DevopsEnvironmentUpdateDTO update(DevopsEnvironmentUpdateDTO devopsEnvironmentUpdateDTO, Long projectId) {
        DevopsEnvironmentE devopsEnvironmentE = ConvertHelper.convert(
                devopsEnvironmentUpdateDTO, DevopsEnvironmentE.class);
        Long clusterId = devopsEnviromentRepository.baseQueryById(devopsEnvironmentUpdateDTO.getId()).getClusterE().getId();
        devopsEnvironmentE.initDevopsClusterEById(clusterId);
        devopsEnvironmentE.initProjectE(projectId);
        List<DevopsEnvironmentE> devopsEnvironmentES = devopsEnviromentRepository
                .baseListByProjectIdAndActive(projectId, true);
        DevopsEnvironmentE beforeDevopsEnvironmentE = devopsEnviromentRepository
                .baseQueryById(devopsEnvironmentUpdateDTO.getId());
        List<Long> ids = new ArrayList<>();
        //更新环境,默认组到环境组,此时将默认组sequence重新排列,环境在所选新环境组中环境sequence递增
        if (devopsEnvironmentUpdateDTO.getDevopsEnvGroupId() != null) {
            if (!devopsEnvironmentUpdateDTO.getDevopsEnvGroupId().equals(beforeDevopsEnvironmentE.getDevopsEnvGroupId())) {
                if (beforeDevopsEnvironmentE.getDevopsEnvGroupId() == null) {
                    ids = devopsEnvironmentES.stream().filter(devopsEnvironmentE1 ->
                            devopsEnvironmentE1.getDevopsEnvGroupId() == null)
                            .sorted(Comparator.comparing(DevopsEnvironmentE::getSequence)).map(
                                    DevopsEnvironmentE::getId)
                            .collect(Collectors.toList());
                }
                //更新环境,环境组到环境组,此时将初始环境组sequence重新排列,环境在所选新环境组中环境sequence递增
                else if (beforeDevopsEnvironmentE.getDevopsEnvGroupId() != null) {
                    ids = devopsEnvironmentES.stream().filter(devopsEnvironmentE1 ->
                            beforeDevopsEnvironmentE.getDevopsEnvGroupId()
                                    .equals(devopsEnvironmentE1.getDevopsEnvGroupId()))
                            .sorted(Comparator.comparing(DevopsEnvironmentE::getSequence)).map(
                                    DevopsEnvironmentE::getId)
                            .collect(Collectors.toList());
                }
                ids.remove(devopsEnvironmentUpdateDTO.getId());
                sort(ids.toArray(new Long[ids.size()]));
                devopsEnvironmentE.initSequence(devopsEnvironmentES.stream().filter(devopsEnvironmentE1 ->
                        (devopsEnvironmentUpdateDTO.getDevopsEnvGroupId())
                                .equals(devopsEnvironmentE1.getDevopsEnvGroupId())).collect(Collectors.toList()));
            }
        } else {
            //更新环境,环境组到默认组
            if (beforeDevopsEnvironmentE.getDevopsEnvGroupId() != null) {
                ids = devopsEnvironmentES.stream().filter(devopsEnvironmentE1 ->
                        beforeDevopsEnvironmentE.getDevopsEnvGroupId()
                                .equals(devopsEnvironmentE1.getDevopsEnvGroupId()))
                        .sorted(Comparator.comparing(DevopsEnvironmentE::getSequence)).map(
                                DevopsEnvironmentE::getId)
                        .collect(Collectors.toList());
                ids.remove(devopsEnvironmentUpdateDTO.getId());
                //此时将初始环境组sequence重新排列
                sort(ids.toArray(new Long[ids.size()]));
                //环境默认环境组中环境sequence递增
                devopsEnvironmentE.initSequence(devopsEnvironmentES.stream().filter(devopsEnvironmentE1 ->
                        devopsEnvironmentE1.getDevopsEnvGroupId() == null).collect(Collectors.toList()));
            }
        }
        if (devopsEnvironmentUpdateDTO.getClusterId() != null && !devopsEnvironmentUpdateDTO.getClusterId()
                .equals(beforeDevopsEnvironmentE.getClusterE().getId())) {
            agentCommandService.initCluster(devopsEnvironmentUpdateDTO.getClusterId());
        }
        return ConvertHelper.convert(devopsEnviromentRepository.baseUpdate(
                devopsEnvironmentE), DevopsEnvironmentUpdateDTO.class);
    }

    @Override
    public DevopsEnvGroupEnvsDTO sort(Long[] environmentIds) {
        DevopsEnvGroupEnvsDTO devopsEnvGroupEnvsDTO = new DevopsEnvGroupEnvsDTO();
        List<DevopsEnvironmentE> devopsEnvironmentES = Arrays.stream(environmentIds)
                .map(id -> devopsEnviromentRepository.baseQueryById(id))
                .collect(Collectors.toList());
        long sequence = 1L;
        for (DevopsEnvironmentE devopsEnvironmentE : devopsEnvironmentES) {
            devopsEnvironmentE.setSequence(sequence);
            devopsEnviromentRepository.baseUpdate(devopsEnvironmentE);
            sequence = sequence + 1;
        }
        List<Long> connectedEnvList = clusterConnectionHandler.getConnectedEnvList();
        List<Long> upgradeClusterList = clusterConnectionHandler.getUpdatedEnvList();

        devopsEnvironmentES.forEach(t ->
                setEnvStatus(connectedEnvList, upgradeClusterList, t)
        );
        if (!devopsEnvironmentES.isEmpty()) {
            DevopsEnvGroupE devopsEnvGroupE = new DevopsEnvGroupE();
            if (devopsEnvironmentES.get(0).getDevopsEnvGroupId() != null) {
                devopsEnvGroupE = devopsEnvGroupRepository.baseQuery(devopsEnvironmentES.get(0).getDevopsEnvGroupId());
            }
            devopsEnvGroupEnvsDTO.setDevopsEnviromentRepDTOs(ConvertHelper.convertList(devopsEnvironmentES,
                    DevopsEnviromentRepDTO.class));
            devopsEnvGroupEnvsDTO.setDevopsEnvGroupName(devopsEnvGroupE.getName());
            devopsEnvGroupEnvsDTO.setDevopsEnvGroupId(devopsEnvGroupE.getId());
        }
        return devopsEnvGroupEnvsDTO;
    }

    private void setEnvStatus(List<Long> connectedEnvList, List<Long> upgradeEnvList, DevopsEnvironmentE t) {
        if (connectedEnvList.contains(t.getClusterE().getId()) && upgradeEnvList.contains(t.getClusterE().getId())) {
            t.initConnect(true);
        } else {
            t.initConnect(false);
        }
    }


    @Override
    public void retryGitOps(Long envId) {
        DevopsEnvironmentE devopsEnvironmentE = devopsEnviromentRepository.baseQueryById(envId);
        UserAttrE userAttrE = userAttrRepository.baseQueryById(GitUserNameUtil.getUserId().longValue());
        if (userAttrE == null) {
            throw new CommonException("error.gitlab.user.sync.failed");
        }
        CommitDTO commitDO = gitlabProjectRepository.listCommits(devopsEnvironmentE.getGitlabEnvProjectId().intValue(), userAttrE.getGitlabUserId().intValue(), 1, 1).get(0);
        PushWebHookDTO pushWebHookDTO = new PushWebHookDTO();
        pushWebHookDTO.setCheckoutSha(commitDO.getId());
        pushWebHookDTO.setUserId(userAttrE.getGitlabUserId().intValue());
        pushWebHookDTO.setProjectId(devopsEnvironmentE.getGitlabEnvProjectId().intValue());
        CommitVO commitDTO = new CommitVO();
        commitDTO.setId(commitDO.getId());
        commitDTO.setTimestamp(commitDO.getTimestamp());
        pushWebHookDTO.setCommits(Arrays.asList(commitDTO));

        //当环境总览第一阶段为空，第一阶段的commit不是最新commit, 第一阶段和第二阶段commit不一致时，可以重新触发gitOps
        if (devopsEnvironmentE.getSagaSyncCommit() == null) {
            devopsGitService.fileResourceSyncSaga(pushWebHookDTO, devopsEnvironmentE.getToken());
        } else {
            DevopsEnvCommitVO sagaSyncCommit = devopsEnvCommitRepository.baseQuery(devopsEnvironmentE.getSagaSyncCommit());
            if (!devopsEnvironmentE.getSagaSyncCommit().equals(devopsEnvironmentE.getDevopsSyncCommit()) || !sagaSyncCommit.getCommitSha().equals(commitDO.getId())) {
                devopsGitService.fileResourceSyncSaga(pushWebHookDTO, devopsEnvironmentE.getToken());
            }
        }
    }

    @Override
    public void checkCode(Long projectId, Long clusterId, String code) {
        DevopsEnvironmentE devopsEnvironmentE = DevopsEnvironmentFactory.createDevopsEnvironmentE();
        DevopsClusterE devopsClusterE = devopsClusterRepository.baseQuery(clusterId);
        devopsEnvironmentE.initProjectE(projectId);
        devopsEnvironmentE.initDevopsClusterEById(clusterId);
        devopsEnvironmentE.setCode(code);
        if (devopsClusterE.getNamespaces() != null) {
            JSONArray.parseArray(devopsClusterE.getNamespaces(), String.class).forEach(namespace -> {
                if (namespace.equals(code)) {
                    throw new CommonException("error.code.exist");
                }
            });
        }
        devopsEnviromentRepository.baseCheckCode(devopsEnvironmentE);
    }

    @Override
    public List<DevopsEnviromentRepDTO> listByProjectId(Long projectId, Long appId) {
        List<DevopsEnviromentRepDTO> devopsEnviromentRepDTOList = listByProjectIdAndActive(projectId, true);

        if (appId == null) {
            return devopsEnviromentRepDTOList.stream().filter(t ->
                    applicationInstanceRepository.selectByEnvId(t.getId()).stream()
                            .anyMatch(applicationInstanceE ->
                                    applicationInstanceE.getStatus().equals(InstanceStatus.RUNNING.getStatus())))
                    .collect(Collectors.toList());
        } else {
            return devopsEnviromentRepDTOList.stream().filter(t ->
                    applicationInstanceRepository.selectByEnvId(t.getId()).stream()
                            .anyMatch(applicationInstanceE ->
                                    applicationInstanceE.getStatus().equals(InstanceStatus.RUNNING.getStatus())
                                            && applicationInstanceE.getApplicationE().getId().equals(appId)))
                    .collect(Collectors.toList());
        }
    }

    @Override
    public void handleCreateEnvSaga(GitlabProjectPayload gitlabProjectPayload) {
        DevopsProjectVO gitlabGroupE = devopsProjectRepository.baseQueryByGitlabEnvGroupId(
                TypeUtil.objToInteger(gitlabProjectPayload.getGroupId()));
        DevopsEnvironmentE devopsEnvironmentE = devopsEnviromentRepository
                .baseQueryByClusterIdAndCode(gitlabProjectPayload.getClusterId(), gitlabProjectPayload.getPath());
        ProjectVO projectE = iamRepository.queryIamProject(gitlabGroupE.getProjectE().getId());
        OrganizationVO organization = iamRepository.queryOrganizationById(projectE.getOrganization().getId());

        GitlabProjectDTO gitlabProjectDO = gitlabRepository.getProjectByName(organization.getCode()
                + "-" + projectE.getCode() + "-gitops", devopsEnvironmentE.getCode(), gitlabProjectPayload.getUserId());
        if (gitlabProjectDO.getId() == null) {
            gitlabProjectDO = gitlabRepository.createProject(
                    gitlabProjectPayload.getGroupId(),
                    gitlabProjectPayload.getPath(),
                    gitlabProjectPayload.getUserId(),
                    false);
        }
        devopsEnvironmentE.initGitlabEnvProjectId(TypeUtil.objToLong(gitlabProjectDO.getId()));
        if (gitlabRepository.getDeployKeys(gitlabProjectDO.getId(), gitlabProjectPayload.getUserId()).isEmpty()) {
            gitlabRepository.createDeployKey(
                    gitlabProjectDO.getId(),
                    gitlabProjectPayload.getPath(),
                    devopsEnvironmentE.getEnvIdRsaPub(),
                    true,
                    gitlabProjectPayload.getUserId());
        }
        ProjectHookDTO projectHookDTO = ProjectHookDTO.allHook();
        projectHookDTO.setEnableSslVerification(true);
        projectHookDTO.setProjectId(gitlabProjectDO.getId());
        projectHookDTO.setToken(devopsEnvironmentE.getToken());
        String uri = !gatewayUrl.endsWith("/") ? gatewayUrl + "/" : gatewayUrl;
        uri += "devops/webhook/git_ops";
        projectHookDTO.setUrl(uri);
        List<ProjectHookDTO> projectHookDTOS = gitlabRepository.getHooks(gitlabProjectDO.getId(),
                gitlabProjectPayload.getUserId());
        if (projectHookDTOS == null || projectHookDTOS.isEmpty()) {
            devopsEnvironmentE.initHookId(TypeUtil.objToLong(gitlabRepository.createWebHook(
                    gitlabProjectDO.getId(), gitlabProjectPayload.getUserId(), projectHookDTO).getId()));
        } else {
            devopsEnvironmentE.initHookId(TypeUtil.objToLong(projectHookDTOS.get(0).getId()));
        }
        if (!gitlabRepository.getFile(gitlabProjectDO.getId(), MASTER, README)) {
            gitlabRepository.createFile(gitlabProjectDO.getId(),
                    README, README_CONTENT, "ADD README", gitlabProjectPayload.getUserId());
        }
        // 创建环境时初始化用户权限，分为gitlab权限和devops环境用户表权限
        initUserPermissionWhenCreatingEnv(gitlabProjectPayload, devopsEnvironmentE.getId(),
                TypeUtil.objToLong(gitlabProjectDO.getId()), projectE.getId());
        devopsEnvironmentE.initSynchro(true);
        devopsEnviromentRepository.baseUpdate(devopsEnvironmentE);
    }

    private void initUserPermissionWhenCreatingEnv(GitlabProjectPayload gitlabProjectPayload, Long envId,
                                                   Long gitlabProjectId, Long projectId) {

        List<Long> userIds = gitlabProjectPayload.getUserIds();
        // 获取项目下所有项目成员

        PageInfo<UserVO> allProjectMemberPage = getMembersFromProject(new PageRequest(0, 0), projectId, "");

        // 所有项目成员中有权限的
        if (userIds != null && !userIds.isEmpty()) {
            allProjectMemberPage.getList().stream().filter(e -> userIds.contains(e.getId())).forEach(e -> {
                Long userId = e.getId();
                String loginName = e.getLoginName();
                String realName = e.getRealName();
                UserAttrE userAttrE = userAttrRepository.baseQueryById(userId);
                Long gitlabUserId = userAttrE.getGitlabUserId();


                GitlabMemberE gitlabGroupMemberE = gitlabGroupMemberRepository.getUserMemberByUserId(gitlabProjectPayload.getGroupId(), TypeUtil.objToInteger(gitlabUserId));
                if (gitlabGroupMemberE != null) {
                    gitlabGroupMemberRepository.deleteMember(gitlabProjectPayload.getGroupId(), TypeUtil.objToInteger(gitlabUserId));
                }

                // 当项目不存在用户权限纪录时(防止失败重试时报成员已存在异常)，添加gitlab用户权限
                GitlabMemberE gitlabMemberE = gitlabProjectRepository.getProjectMember(gitlabProjectId.intValue(), TypeUtil.objToInteger(gitlabUserId));
                if (gitlabMemberE == null || gitlabMemberE.getId() == null) {
                    MemberVO memberDTO = new MemberVO(TypeUtil.objToInteger(gitlabUserId), 40, "");
                    gitlabRepository.addMemberIntoProject(TypeUtil.objToInteger(gitlabProjectId), memberDTO);
                }
                // 添加devops数据库记录
                devopsEnvUserPermissionRepository
                        .baseCreate(new DevopsEnvUserPermissionE(loginName, userId, realName, envId, true));
            });
        }
    }

    @Override
    public EnvSyncStatusDTO queryEnvSyncStatus(Long projectId, Long envId) {
        ProjectVO projectE = iamRepository.queryIamProject(projectId);
        OrganizationVO organization = iamRepository.queryOrganizationById(projectE.getOrganization().getId());
        DevopsEnvironmentE devopsEnvironmentE = devopsEnviromentRepository.baseQueryById(envId);
        EnvSyncStatusDTO envSyncStatusDTO = new EnvSyncStatusDTO();
        if (devopsEnvironmentE.getAgentSyncCommit() != null) {
            envSyncStatusDTO.setAgentSyncCommit(devopsEnvCommitRepository
                    .baseQuery(devopsEnvironmentE.getAgentSyncCommit()).getCommitSha());
        }
        if (devopsEnvironmentE.getDevopsSyncCommit() != null) {
            envSyncStatusDTO.setDevopsSyncCommit(devopsEnvCommitRepository
                    .baseQuery(devopsEnvironmentE.getDevopsSyncCommit())
                    .getCommitSha());
        }
        if (devopsEnvironmentE.getSagaSyncCommit() != null) {
            envSyncStatusDTO.setSagaSyncCommit(devopsEnvCommitRepository
                    .baseQuery(devopsEnvironmentE.getSagaSyncCommit()).getCommitSha());
        }

        gitlabUrl = gitlabUrl.endsWith("/") ? gitlabUrl.substring(0, gitlabUrl.length() - 1) : gitlabUrl;
        envSyncStatusDTO.setCommitUrl(String.format("%s/%s-%s-gitops/%s/commit/",
                gitlabUrl, organization.getCode(), projectE.getCode(),
                devopsEnvironmentE.getCode()));
        return envSyncStatusDTO;
    }

    @Override
    public PageInfo<DevopsEnvUserPermissionVO> listUserPermissionByEnvId(Long projectId, PageRequest pageRequest,
                                                                         String searchParams, Long envId) {
        if (envId == null) {
            // 根据项目成员id查询项目下所有的项目成员
            PageInfo<UserVO> allProjectMemberPage = getMembersFromProject(pageRequest, projectId, searchParams);

            List<DevopsEnvUserPermissionVO> allProjectMemberList = new ArrayList<>();
            PageInfo<DevopsEnvUserPermissionVO> devopsEnvUserPermissionDTOPage = new PageInfo<>();
            allProjectMemberPage.getList().forEach(e -> {
                DevopsEnvUserPermissionVO devopsEnvUserPermissionDTO = new DevopsEnvUserPermissionVO();
                devopsEnvUserPermissionDTO.setIamUserId(e.getId());
                devopsEnvUserPermissionDTO.setLoginName(e.getLoginName());
                devopsEnvUserPermissionDTO.setRealName(e.getRealName());
                allProjectMemberList.add(devopsEnvUserPermissionDTO);
            });
            BeanUtils.copyProperties(allProjectMemberPage, devopsEnvUserPermissionDTOPage);
            devopsEnvUserPermissionDTOPage.setList(allProjectMemberList);
            return devopsEnvUserPermissionDTOPage;
        } else {
            List<DevopsEnvUserPermissionVO> retureUsersDTOList = new ArrayList<>();
            // 普通分页需要带上iam中的所有项目成员，如果iam中的项目所有者也带有项目成员的身份，则需要去掉
            PageInfo<UserVO> allProjectMemberPage = getMembersFromProject(pageRequest, projectId, searchParams);
            allProjectMemberPage.getList().forEach(e -> {
                DevopsEnvUserPermissionVO devopsEnvUserPermissionDTO = new DevopsEnvUserPermissionVO();
                devopsEnvUserPermissionDTO.setIamUserId(e.getId());
                devopsEnvUserPermissionDTO.setLoginName(e.getLoginName());
                devopsEnvUserPermissionDTO.setRealName(e.getRealName());
                retureUsersDTOList.add(devopsEnvUserPermissionDTO);
            });
            PageInfo<DevopsEnvUserPermissionVO> devopsEnvUserPermissionDTOPage = new PageInfo<>();
            BeanUtils.copyProperties(allProjectMemberPage, devopsEnvUserPermissionDTOPage);
            devopsEnvUserPermissionDTOPage.setList(retureUsersDTOList);
            return devopsEnvUserPermissionDTOPage;
        }
    }

    @Override
    public List<DevopsEnvUserPermissionVO> listAllUserPermission(Long envId) {
        return devopsEnvUserPermissionRepository.baseListByEnvId(envId);
    }

    @Override
    public Boolean updateEnvUserPermission(Long envId, List<Long> userIds) {
        UpdateUserPermissionService updateEnvUserPermissionService = new UpdateEnvUserPermissionServiceImpl();
        return updateEnvUserPermissionService.updateUserPermission(null, envId, userIds, null);
    }

    private PageInfo<UserVO> getMembersFromProject(PageRequest pageRequest, Long projectId, String searchParams) {
        RoleAssignmentSearchDTO roleAssignmentSearchDTO = new RoleAssignmentSearchDTO();
        if (searchParams != null && !"".equals(searchParams)) {
            Map maps = gson.fromJson(searchParams, Map.class);
            Map<String, Object> searchParamMap = TypeUtil.cast(maps.get(TypeUtil.SEARCH_PARAM));
            String param = TypeUtil.cast(maps.get(TypeUtil.PARAM));
            roleAssignmentSearchDTO.setParam(new String[]{param});
            if (searchParamMap.get("loginName") != null) {
                String loginName = TypeUtil.objToString(searchParamMap.get("loginName"));
                String subLogin = loginName.substring(1, loginName.length() - 1);
                roleAssignmentSearchDTO.setLoginName(subLogin);
            }
            if (searchParamMap.get("realName") != null) {
                String realName = TypeUtil.objToString(searchParamMap.get("realName"));
                String subReal = realName.substring(1, realName.length() - 1);
                roleAssignmentSearchDTO.setRealName(subReal);
            }
        }
        // 获取项目所有者角色id和数量
        Long ownerId = iamRepository.queryRoleIdByCode(PROJECT_OWNER);
        // 获取项目成员id
        Long memberId = iamRepository.queryRoleIdByCode(PROJECT_MEMBER);
        // 所有项目成员，可能还带有项目所有者的角色，需要过滤
        PageInfo<UserVO> allMemberWithOtherUsersPage = iamRepository

                .pagingQueryUsersByRoleIdOnProjectLevel(new PageRequest(0, 0), roleAssignmentSearchDTO,
                        memberId, projectId, false);
        // 如果项目成员查出来为空，则直接返回空列表
        if (allMemberWithOtherUsersPage.getList().isEmpty()) {
            return allMemberWithOtherUsersPage;
        }
        // 所有项目所有者
        PageInfo<UserVO> allOwnerUsersPage = iamRepository

                .pagingQueryUsersByRoleIdOnProjectLevel(new PageRequest(0, 0), roleAssignmentSearchDTO,
                        ownerId, projectId, false);
        // 如果项目所有者查出来为空，则返回之前的项目成员列表
        if (allOwnerUsersPage.getList().isEmpty()) {
            return allMemberWithOtherUsersPage;
        } else {
            // 否则过滤项目成员中含有项目所有者的人
            List<UserVO> returnUserDTOList = allMemberWithOtherUsersPage.getList().stream()
                    .filter(e -> !allOwnerUsersPage.getList().contains(e)).collect(Collectors.toList());
            // 设置过滤后的分页显示参数
            allMemberWithOtherUsersPage.setList(returnUserDTOList);
            allMemberWithOtherUsersPage.setPageSize(pageRequest.getSize());
            allMemberWithOtherUsersPage.setTotal(returnUserDTOList.size());
            allMemberWithOtherUsersPage.setPageNum(pageRequest.getPage());
            if (returnUserDTOList.size() < pageRequest.getSize() * pageRequest.getPage()) {
                allMemberWithOtherUsersPage.setSize(TypeUtil.objToInt(returnUserDTOList.size()) - (pageRequest.getSize() * (pageRequest.getPage() - 1)));
            } else {
                allMemberWithOtherUsersPage.setSize(pageRequest.getSize());
            }

            return allMemberWithOtherUsersPage;
        }

    }

    private void setPermission(DevopsEnvironmentE devopsEnvironmentE, List<Long> permissionEnvIds,
                               Boolean isProjectOwner) {
        if (permissionEnvIds.contains(devopsEnvironmentE.getId()) || isProjectOwner) {
            devopsEnvironmentE.setPermission(true);
        } else {
            devopsEnvironmentE.setPermission(false);
        }
    }

    @Override
    @Transactional
    public void deleteDeactivatedEnvironment(Long envId) {
        DevopsEnvironmentDTO devopsEnvironmentDTO = baseQueryById(envId);
        // 删除环境对应的实例
        applicationInstanceRepository.selectByEnvId(envId).forEach(instanceE ->
                devopsEnvCommandRepository.baseListByObject(HelmObjectKind.INSTANCE.toValue(), instanceE.getId()).forEach(t -> devopsEnvCommandRepository.baseDeleteByEnvCommandId(t)));
        applicationInstanceRepository.deleteAppInstanceByEnvId(envId);
        // 删除环境对应的域名、域名路径
        devopsIngressRepository.baseListByEnvId(envId).forEach(ingressE ->
                devopsEnvCommandRepository.baseListByObjectAll(HelmObjectKind.INGRESS.toValue(), ingressE.getId()).forEach(t -> devopsEnvCommandRepository.baseDeleteCommandById(t)));
        devopsIngressRepository.deleteIngressAndIngressPathByEnvId(envId);
        // 删除环境对应的网络和网络实例
        devopsServiceRepository.baseListByEnvId(envId).forEach(serviceE ->
                devopsEnvCommandRepository.baseListByObjectAll(HelmObjectKind.SERVICE.toValue(), serviceE.getId()).forEach(t -> devopsEnvCommandRepository.baseDeleteCommandById(t)));
        devopsServiceRepository.baseDeleteServiceAndInstanceByEnvId(envId);

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
        if (devopsEnvironmentE.getClusterE().getId() != null) {
            agentCommandService.deleteEnv(envId, devopsEnvironmentE.getCode(), devopsEnvironmentE.getClusterE().getId());
        }
    }

    @Override
    public List<DevopsClusterRepDTO> listDevopsCluster(Long projectId) {
        ProjectVO projectE = iamRepository.queryIamProject(projectId);
        List<DevopsClusterRepDTO> devopsClusterRepDTOS = ConvertHelper.convertList(devopsClusterRepository.baseListByProjectId(projectId, projectE.getOrganization().getId()), DevopsClusterRepDTO.class);
        List<Long> connectedClusterList = clusterConnectionHandler.getConnectedEnvList();
        List<Long> upgradeClusterList = clusterConnectionHandler.getUpdatedEnvList();
        devopsClusterRepDTOS.forEach(t -> {
            if (connectedClusterList.contains(t.getId()) && upgradeClusterList.contains(t.getId())) {
                t.setConnect(true);
            } else {
                t.setConnect(false);
            }
        });
        return devopsClusterRepDTOS;
    }

    @Override
    @Saga(code = "devops-set-env-err",
            description = "devops创建环境失败(devops set env status create err)", inputSchema = "{}")
    public void setEnvErrStatus(String data, Long projectId) {
        sagaClient.startSaga("devops-set-env-err", new StartInstanceDTO(data, "", "", ResourceLevel.PROJECT.value(), projectId));
    }

    @Override
    public DevopsEnviromentRepDTO queryByCode(Long clusterId, String code) {
        return ConvertHelper.convert(devopsEnviromentRepository.baseQueryByProjectIdAndCode(clusterId, code), DevopsEnviromentRepDTO.class);
    }

    @Override
    public void initMockService(SagaClient sagaClient) {
        this.sagaClient = sagaClient;
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

}
