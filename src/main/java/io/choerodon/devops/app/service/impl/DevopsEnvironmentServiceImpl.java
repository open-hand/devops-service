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
import io.choerodon.core.convertor.ConvertHelper;
import io.choerodon.core.domain.Page;
import io.choerodon.core.exception.CommonException;
import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.devops.api.dto.*;
import io.choerodon.devops.api.dto.gitlab.MemberDTO;
import io.choerodon.devops.api.dto.iam.UserDTO;
import io.choerodon.devops.api.validator.DevopsEnvironmentValidator;
import io.choerodon.devops.app.service.DeployMsgHandlerService;
import io.choerodon.devops.app.service.DevopsEnvironmentService;
import io.choerodon.devops.domain.application.entity.*;
import io.choerodon.devops.domain.application.entity.gitlab.GitlabMemberE;
import io.choerodon.devops.domain.application.entity.iam.UserE;
import io.choerodon.devops.domain.application.event.GitlabProjectPayload;
import io.choerodon.devops.domain.application.factory.DevopsEnvironmentFactory;
import io.choerodon.devops.domain.application.repository.*;
import io.choerodon.devops.domain.application.valueobject.Organization;
import io.choerodon.devops.domain.application.valueobject.ProjectHook;
import io.choerodon.devops.domain.service.DeployService;
import io.choerodon.devops.domain.service.UpdateUserPermissionService;
import io.choerodon.devops.domain.service.impl.UpdateEnvUserPermissionServiceImpl;
import io.choerodon.devops.infra.common.util.*;
import io.choerodon.devops.infra.common.util.enums.AccessLevel;
import io.choerodon.devops.infra.common.util.enums.HelmObjectKind;
import io.choerodon.devops.infra.common.util.enums.InstanceStatus;
import io.choerodon.devops.infra.dataobject.gitlab.GitlabProjectDO;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
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
    private EnvUtil envUtil;
    @Autowired
    private GitUtil gitUtil;
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
    private DeployService deployService;
    @Autowired
    private GitlabProjectRepository gitlabProjectRepository;
    @Autowired
    private DeployMsgHandlerService deployMsgHandlerService;
    @Autowired
    private DevopsEnvCommandRepository devopsEnvCommandRepository;
    @Autowired
    private GitlabGroupMemberRepository gitlabGroupMemberRepository;

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
        ProjectE projectE = iamRepository.queryIamProject(projectId);
        Organization organization = iamRepository.queryOrganizationById(projectE.getOrganization().getId());

        UserAttrE userAttrE = userAttrRepository.queryById(TypeUtil.objToLong(GitUserNameUtil.getUserId()));
        if (userAttrE == null) {
            throw new CommonException("error.gitlab.user.sync.failed");
        }

        // 查询创建应用所在的gitlab应用组
        DevopsProjectE devopsProjectE = devopsProjectRepository.queryDevopsProject(projectId);
        GitlabMemberE gitlabMemberE = gitlabGroupMemberRepository.getUserMemberByUserId(
                TypeUtil.objToInteger(devopsProjectE.getDevopsEnvGroupId()),
                TypeUtil.objToInteger(userAttrE.getGitlabUserId()));
        if (gitlabMemberE == null || gitlabMemberE.getAccessLevel() != AccessLevel.OWNER.toValue()) {
            throw new CommonException("error.user.not.owner");
        }

        List<DevopsEnvironmentE> devopsEnvironmentES = devopsEnviromentRepository
                .queryByprojectAndActive(projectId, true);
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
        Long envId = devopsEnviromentRepository.create(devopsEnvironmentE).getId();
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
            deployService.initEnv(devopsEnvironmentE, devopsEnviromentDTO.getClusterId());
        } catch (JsonProcessingException e) {
            throw new CommonException(e.getMessage(), e);
        }
    }

    @Override
    public List<DevopsEnvGroupEnvsDTO> listDevopsEnvGroupEnvs(Long projectId, Boolean active) {
        List<DevopsEnvGroupEnvsDTO> devopsEnvGroupEnvsDTOS = new ArrayList<>();
        List<Long> connectedClusterList = envUtil.getConnectedEnvList();
        List<Long> upgradeClusterList = envUtil.getUpdatedEnvList();
        List<DevopsEnvironmentE> devopsEnvironmentES = devopsEnviromentRepository
                .queryByprojectAndActive(projectId, active).stream().peek(t ->
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
        List<DevopsEnvGroupE> devopsEnvGroupES = devopsEnvGroupRepository.listByProjectId(projectId);
        devopsEnviromentRepDTOS.forEach(devopsEnviromentRepDTO -> {
            DevopsClusterE devopsClusterE = devopsClusterRepository.query(devopsEnviromentRepDTO.getClusterId());
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
                devopsEnvGroupE = devopsEnvGroupRepository.query(key);
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
                .listByUserId(TypeUtil.objToLong(GitUserNameUtil.getUserId())).stream()
                .filter(DevopsEnvUserPermissionE::getPermitted)
                .map(DevopsEnvUserPermissionE::getEnvId).collect(Collectors.toList());
        ProjectE projectE = iamRepository.queryIamProject(projectId);
        // 查询当前用户是否为项目所有者
        Boolean isProjectOwner = iamRepository
                .isProjectOwner(TypeUtil.objToLong(GitUserNameUtil.getUserId()), projectE);

        List<Long> connectedClusterList = envUtil.getConnectedEnvList();
        List<Long> upgradeClusterList = envUtil.getUpdatedEnvList();
        List<DevopsEnvironmentE> devopsEnvironmentES = devopsEnviromentRepository
                .queryByprojectAndActive(projectId, active).stream()
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
        List<Long> envList = devopsServiceRepository.selectDeployedEnv();
        return listByProjectIdAndActive(projectId, true).stream().filter(t ->
                envList.contains(t.getId())).collect(Collectors.toList());
    }

    @Override
    public Boolean activeEnvironment(Long projectId, Long environmentId, Boolean active) {
        DevopsEnvironmentE devopsEnvironmentE = devopsEnviromentRepository.queryById(environmentId);

        List<Long> upgradeClusterList = envUtil.getUpdatedEnvList();
        List<Long> connectedClusterList = envUtil.getConnectedEnvList();
        setEnvStatus(connectedClusterList, upgradeClusterList, devopsEnvironmentE);
        if (!active && devopsEnvironmentE.getConnect()) {
            devopsEnvironmentValidator.checkEnvCanDisabled(environmentId);
        }
        List<DevopsEnvironmentE> devopsEnvironmentES = devopsEnviromentRepository
                .queryByprojectAndActive(projectId, true);
        devopsEnvironmentE.setActive(active);
        //启用环境，原环境不在环境组内或者原环境所在环境组被删除，则序列在默认组内环境递增，原环境在环境组内，则序列在环境组内环境递增
        if (active) {
            DevopsEnvGroupE devopsEnvGroupE = devopsEnvGroupRepository.query(devopsEnvironmentE.getDevopsEnvGroupId());
            if (devopsEnvironmentE.getDevopsEnvGroupId() == null || devopsEnvGroupE == null) {
                devopsEnvironmentE.setDevopsEnvGroupId(null);
                devopsEnviromentRepository.update(devopsEnvironmentE);
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
        devopsEnviromentRepository.update(devopsEnvironmentE);
        return true;
    }

    @Override
    public DevopsEnvironmentUpdateDTO query(Long environmentId) {
        return ConvertHelper.convert(devopsEnviromentRepository
                .queryById(environmentId), DevopsEnvironmentUpdateDTO.class);
    }

    @Override
    public DevopsEnvironmentUpdateDTO update(DevopsEnvironmentUpdateDTO devopsEnvironmentUpdateDTO, Long projectId) {
        DevopsEnvironmentE devopsEnvironmentE = ConvertHelper.convert(
                devopsEnvironmentUpdateDTO, DevopsEnvironmentE.class);
        Long clusterId = devopsEnviromentRepository.queryById(devopsEnvironmentUpdateDTO.getId()).getClusterE().getId();
        devopsEnvironmentE.initDevopsClusterEById(clusterId);
        devopsEnvironmentE.initProjectE(projectId);
        List<DevopsEnvironmentE> devopsEnvironmentES = devopsEnviromentRepository
                .queryByprojectAndActive(projectId, true);
        DevopsEnvironmentE beforeDevopsEnvironmentE = devopsEnviromentRepository
                .queryById(devopsEnvironmentUpdateDTO.getId());
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
            deployService.initCluster(devopsEnvironmentUpdateDTO.getClusterId());
        }
        return ConvertHelper.convert(devopsEnviromentRepository.update(
                devopsEnvironmentE), DevopsEnvironmentUpdateDTO.class);
    }

    @Override
    public DevopsEnvGroupEnvsDTO sort(Long[] environmentIds) {
        DevopsEnvGroupEnvsDTO devopsEnvGroupEnvsDTO = new DevopsEnvGroupEnvsDTO();
        List<DevopsEnvironmentE> devopsEnvironmentES = Arrays.stream(environmentIds)
                .map(id -> devopsEnviromentRepository.queryById(id))
                .collect(Collectors.toList());
        long sequence = 1L;
        for (DevopsEnvironmentE devopsEnvironmentE : devopsEnvironmentES) {
            devopsEnvironmentE.setSequence(sequence);
            devopsEnviromentRepository.update(devopsEnvironmentE);
            sequence = sequence + 1;
        }
        List<Long> connectedEnvList = envUtil.getConnectedEnvList();
        List<Long> upgradeClusterList = envUtil.getUpdatedEnvList();

        devopsEnvironmentES.forEach(t ->
                setEnvStatus(connectedEnvList, upgradeClusterList, t)
        );
        if (!devopsEnvironmentES.isEmpty()) {
            DevopsEnvGroupE devopsEnvGroupE = new DevopsEnvGroupE();
            if (devopsEnvironmentES.get(0).getDevopsEnvGroupId() != null) {
                devopsEnvGroupE = devopsEnvGroupRepository.query(devopsEnvironmentES.get(0).getDevopsEnvGroupId());
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
    public void checkCode(Long projectId, Long clusterId, String code) {
        DevopsEnvironmentE devopsEnvironmentE = DevopsEnvironmentFactory.createDevopsEnvironmentE();
        DevopsClusterE devopsClusterE = devopsClusterRepository.query(clusterId);
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
        devopsEnviromentRepository.checkCode(devopsEnvironmentE);
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
        DevopsProjectE gitlabGroupE = devopsProjectRepository.queryByEnvGroupId(
                TypeUtil.objToInteger(gitlabProjectPayload.getGroupId()));
        DevopsEnvironmentE devopsEnvironmentE = devopsEnviromentRepository
                .queryByClusterIdAndCode(gitlabProjectPayload.getClusterId(), gitlabProjectPayload.getPath());
        ProjectE projectE = iamRepository.queryIamProject(gitlabGroupE.getProjectE().getId());
        Organization organization = iamRepository.queryOrganizationById(projectE.getOrganization().getId());

        GitlabProjectDO gitlabProjectDO = gitlabRepository.getProjectByName(organization.getCode()
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
        ProjectHook projectHook = ProjectHook.allHook();
        projectHook.setEnableSslVerification(true);
        projectHook.setProjectId(gitlabProjectDO.getId());
        projectHook.setToken(devopsEnvironmentE.getToken());
        String uri = !gatewayUrl.endsWith("/") ? gatewayUrl + "/" : gatewayUrl;
        uri += "devops/webhook/git_ops";
        projectHook.setUrl(uri);
        List<ProjectHook> projectHooks = gitlabRepository.getHooks(gitlabProjectDO.getId(),
                gitlabProjectPayload.getUserId());
        if (projectHooks.isEmpty()) {
            devopsEnvironmentE.initHookId(TypeUtil.objToLong(gitlabRepository.createWebHook(
                    gitlabProjectDO.getId(), gitlabProjectPayload.getUserId(), projectHook).getId()));
        } else {
            devopsEnvironmentE.initHookId(TypeUtil.objToLong(projectHooks.get(0).getId()));
        }
        if (!gitlabRepository.getFile(gitlabProjectDO.getId(), MASTER, README)) {
            gitlabRepository.createFile(gitlabProjectDO.getId(),
                    README, README_CONTENT, "ADD README", gitlabProjectPayload.getUserId());
        }
        // 创建环境时初始化用户权限，分为gitlab权限和devops环境用户表权限
        initUserPermissionWhenCreatingEnv(gitlabProjectPayload, devopsEnvironmentE.getId(),
                TypeUtil.objToLong(gitlabProjectDO.getId()), projectE.getId());
        devopsEnvironmentE.initSynchro(true);
        devopsEnviromentRepository.update(devopsEnvironmentE);
    }

    private void initUserPermissionWhenCreatingEnv(GitlabProjectPayload gitlabProjectPayload, Long envId,
                                                   Long gitlabProjectId, Long projectId) {

        List<Long> userIds = gitlabProjectPayload.getUserIds();
        // 获取项目下所有项目成员
        PageInfo<UserDTO> allProjectMemberPage = getMembersFromProject(new PageRequest(), projectId, "");
        // 所有项目成员中有权限的
        if (userIds != null && !userIds.isEmpty()) {
            allProjectMemberPage.getList().stream().filter(e -> userIds.contains(e.getId())).forEach(e -> {
                Long userId = e.getId();
                String loginName = e.getLoginName();
                String realName = e.getRealName();
                UserAttrE userAttrE = userAttrRepository.queryById(userId);
                Long gitlabUserId = userAttrE.getGitlabUserId();

                // 当项目不存在用户权限纪录时(防止失败重试时报成员已存在异常)，添加gitlab用户权限
                GitlabMemberE gitlabMemberE = gitlabProjectRepository.getProjectMember(gitlabProjectId.intValue(), TypeUtil.objToInteger(gitlabUserId));
                if (gitlabMemberE == null || gitlabMemberE.getId() == null) {
                    MemberDTO memberDTO = new MemberDTO(TypeUtil.objToInteger(gitlabUserId), 40, "");
                    gitlabRepository.addMemberIntoProject(TypeUtil.objToInteger(gitlabProjectId), memberDTO);
                }
                // 添加devops数据库记录
                devopsEnvUserPermissionRepository
                        .create(new DevopsEnvUserPermissionE(loginName, userId, realName, envId, true));
            });
        }
    }

    @Override
    public EnvSyncStatusDTO queryEnvSyncStatus(Long projectId, Long envId) {
        ProjectE projectE = iamRepository.queryIamProject(projectId);
        Organization organization = iamRepository.queryOrganizationById(projectE.getOrganization().getId());
        DevopsEnvironmentE devopsEnvironmentE = devopsEnviromentRepository.queryById(envId);
        EnvSyncStatusDTO envSyncStatusDTO = new EnvSyncStatusDTO();
        if (devopsEnvironmentE.getAgentSyncCommit() != null) {
            envSyncStatusDTO.setAgentSyncCommit(devopsEnvCommitRepository
                    .query(devopsEnvironmentE.getAgentSyncCommit()).getCommitSha());
        }
        if (devopsEnvironmentE.getDevopsSyncCommit() != null) {
            envSyncStatusDTO.setDevopsSyncCommit(devopsEnvCommitRepository
                    .query(devopsEnvironmentE.getDevopsSyncCommit())
                    .getCommitSha());
        }
        if (devopsEnvironmentE.getSagaSyncCommit() != null) {
            envSyncStatusDTO.setSagaSyncCommit(devopsEnvCommitRepository
                    .query(devopsEnvironmentE.getSagaSyncCommit()).getCommitSha());
        }

        gitlabUrl = gitlabUrl.endsWith("/") ? gitlabUrl.substring(0, gitlabUrl.length() - 1) : gitlabUrl;
        envSyncStatusDTO.setCommitUrl(String.format("%s/%s-%s-gitops/%s/commit/",
                gitlabUrl, organization.getCode(), projectE.getCode(),
                devopsEnvironmentE.getCode()));
        return envSyncStatusDTO;
    }

    @Override
    public Page<DevopsEnvUserPermissionDTO> listUserPermissionByEnvId(Long projectId, PageRequest pageRequest,
                                                                      String searchParams, Long envId) {
        if (envId == null) {
            // 根据项目成员id查询项目下所有的项目成员
            PageInfo<UserDTO> allProjectMemberPage = getMembersFromProject(pageRequest, projectId, searchParams);

            List<DevopsEnvUserPermissionDTO> allProjectMemberList = new ArrayList<>();
            Page<DevopsEnvUserPermissionDTO> devopsEnvUserPermissionDTOPage = new Page<>();
            allProjectMemberPage.getList().forEach(e -> {
                DevopsEnvUserPermissionDTO devopsEnvUserPermissionDTO = new DevopsEnvUserPermissionDTO();
                devopsEnvUserPermissionDTO.setIamUserId(e.getId());
                devopsEnvUserPermissionDTO.setLoginName(e.getLoginName());
                devopsEnvUserPermissionDTO.setRealName(e.getRealName());
                allProjectMemberList.add(devopsEnvUserPermissionDTO);
            });
            BeanUtils.copyProperties(allProjectMemberPage, devopsEnvUserPermissionDTOPage);
            devopsEnvUserPermissionDTOPage.setContent(allProjectMemberList);
            return devopsEnvUserPermissionDTOPage;
        } else {
            List<DevopsEnvUserPermissionDTO> retureUsersDTOList = new ArrayList<>();
            // 普通分页需要带上iam中的所有项目成员，如果iam中的项目所有者也带有项目成员的身份，则需要去掉
            PageInfo<UserDTO> allProjectMemberPage = getMembersFromProject(pageRequest, projectId, searchParams);
            allProjectMemberPage.getList().forEach(e -> {
                DevopsEnvUserPermissionDTO devopsEnvUserPermissionDTO = new DevopsEnvUserPermissionDTO();
                devopsEnvUserPermissionDTO.setIamUserId(e.getId());
                devopsEnvUserPermissionDTO.setLoginName(e.getLoginName());
                devopsEnvUserPermissionDTO.setRealName(e.getRealName());
                retureUsersDTOList.add(devopsEnvUserPermissionDTO);
            });
            Page<DevopsEnvUserPermissionDTO> devopsEnvUserPermissionDTOPage = new Page<>();
            BeanUtils.copyProperties(allProjectMemberPage, devopsEnvUserPermissionDTOPage);
            devopsEnvUserPermissionDTOPage.setContent(retureUsersDTOList);
            return devopsEnvUserPermissionDTOPage;
        }
    }

    @Override
    public List<DevopsEnvUserPermissionDTO> listAllUserPermission(Long envId) {
        return devopsEnvUserPermissionRepository.listALlUserPermission(envId);
    }

    @Override
    public Boolean updateEnvUserPermission(Long envId, List<Long> userIds) {
        UpdateUserPermissionService updateEnvUserPermissionService = new UpdateEnvUserPermissionServiceImpl();
        return updateEnvUserPermissionService.updateUserPermission(null, envId, userIds, null);
    }

    private PageInfo<UserDTO> getMembersFromProject(PageRequest pageRequest, Long projectId, String searchParams) {
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
        PageInfo<UserDTO> allMemberWithOtherUsersPage = iamRepository
                .pagingQueryUsersByRoleIdOnProjectLevel(new PageRequest(), roleAssignmentSearchDTO,
                        memberId, projectId, true);
        // 如果项目成员查出来为空，则直接返回空列表
        if (allMemberWithOtherUsersPage.getList().isEmpty()) {
            return allMemberWithOtherUsersPage;
        }
        // 所有项目所有者
        PageInfo<UserDTO> allOwnerUsersPage = iamRepository
                .pagingQueryUsersByRoleIdOnProjectLevel(new PageRequest(), roleAssignmentSearchDTO,
                        ownerId, projectId, false);
        // 如果项目所有者查出来为空，则返回之前的项目成员列表
        if (allOwnerUsersPage.getList().isEmpty()) {
            return allMemberWithOtherUsersPage;
        } else {
            // 否则过滤项目成员中含有项目所有者的人
            List<UserDTO> returnUserDTOList = allMemberWithOtherUsersPage.getList().stream()
                    .filter(e -> !allOwnerUsersPage.getList().contains(e)).collect(Collectors.toList());
            // 设置过滤后的分页显示参数
            allMemberWithOtherUsersPage.setList(returnUserDTOList);
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
        DevopsEnvironmentE devopsEnvironmentE = devopsEnviromentRepository.queryById(envId);
        // 删除环境对应的实例
        applicationInstanceRepository.selectByEnvId(envId).forEach(instanceE->
                devopsEnvCommandRepository.listByObjectAll(HelmObjectKind.INSTANCE.toValue(), instanceE.getId()).forEach(t -> deployMsgHandlerService.deleteCommandById(t)));
        applicationInstanceRepository.deleteAppInstanceByEnvId(envId);
        // 删除环境对应的域名、域名路径
        devopsIngressRepository.listByEnvId(envId).forEach(ingressE->
                devopsEnvCommandRepository.listByObjectAll(HelmObjectKind.INGRESS.toValue(), ingressE.getId()).forEach(t -> deployMsgHandlerService.deleteCommandById(t)));
        devopsIngressRepository.deleteIngressAndIngressPathByEnvId(envId);
        // 删除环境对应的网络和网络实例
        devopsServiceRepository.selectByEnvId(envId).forEach(serviceE->
                devopsEnvCommandRepository.listByObjectAll(HelmObjectKind.SERVICE.toValue(), serviceE.getId()).forEach(t -> deployMsgHandlerService.deleteCommandById(t)));
        devopsServiceRepository.deleteServiceAndInstanceByEnvId(envId);
        // 删除环境
        devopsEnviromentRepository.deleteById(envId);
        // 删除gitlab库, 删除之前查询是否存在
        if (devopsEnvironmentE.getGitlabEnvProjectId() != null) {
            Integer gitlabProjectId = TypeUtil.objToInt(devopsEnvironmentE.getGitlabEnvProjectId());
            GitlabProjectDO gitlabProjectDO = gitlabRepository.getProjectById(gitlabProjectId);
            if (gitlabProjectDO != null && gitlabProjectDO.getId() != null) {
                UserAttrE userAttrE = userAttrRepository.queryById(TypeUtil.objToLong(GitUserNameUtil.getUserId()));
                Integer gitlabUserId = TypeUtil.objToInt(userAttrE.getGitlabUserId());
                gitlabRepository.deleteProject(gitlabProjectId, gitlabUserId);
            }
        }
        // 删除环境命名空间
        if (devopsEnvironmentE.getClusterE().getId() != null) {
            deployService.deleteEnv(envId, devopsEnvironmentE.getCode(), devopsEnvironmentE.getClusterE().getId());
        }
    }

    @Override
    public List<DevopsClusterRepDTO> listDevopsCluster(Long projectId) {
        ProjectE projectE = iamRepository.queryIamProject(projectId);
        List<DevopsClusterRepDTO> devopsClusterRepDTOS = ConvertHelper.convertList(devopsClusterRepository.listByProjectId(projectId, projectE.getOrganization().getId()), DevopsClusterRepDTO.class);
        List<Long> connectedClusterList = envUtil.getConnectedEnvList();
        List<Long> upgradeClusterList = envUtil.getUpdatedEnvList();
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
        return ConvertHelper.convert(devopsEnviromentRepository.queryByProjectIdAndCode(clusterId, code), DevopsEnviromentRepDTO.class);
    }

    @Override
    public void initMockService(SagaClient sagaClient) {
        this.sagaClient = sagaClient;
    }
}
