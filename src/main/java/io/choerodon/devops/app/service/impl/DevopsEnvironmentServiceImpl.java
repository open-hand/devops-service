package io.choerodon.devops.app.service.impl;

import java.io.File;
import java.io.InputStream;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.choerodon.asgard.saga.annotation.Saga;
import io.choerodon.asgard.saga.dto.StartInstanceDTO;
import io.choerodon.asgard.saga.feign.SagaClient;
import io.choerodon.core.convertor.ConvertHelper;
import io.choerodon.core.domain.Page;
import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.dto.*;
import io.choerodon.devops.api.dto.gitlab.MemberDTO;
import io.choerodon.devops.api.dto.iam.RoleDTO;
import io.choerodon.devops.api.dto.iam.UserDTO;
import io.choerodon.devops.api.validator.DevopsEnvironmentValidator;
import io.choerodon.devops.app.service.DevopsEnvironmentService;
import io.choerodon.devops.domain.application.entity.*;
import io.choerodon.devops.domain.application.entity.gitlab.GitlabGroupE;
import io.choerodon.devops.domain.application.entity.gitlab.GitlabGroupMemberE;
import io.choerodon.devops.domain.application.entity.iam.UserE;
import io.choerodon.devops.domain.application.event.GitlabProjectPayload;
import io.choerodon.devops.domain.application.factory.DevopsEnvironmentFactory;
import io.choerodon.devops.domain.application.repository.*;
import io.choerodon.devops.domain.application.valueobject.Organization;
import io.choerodon.devops.domain.application.valueobject.ProjectHook;
import io.choerodon.devops.infra.common.util.*;
import io.choerodon.devops.infra.common.util.enums.InstanceStatus;
import io.choerodon.devops.infra.dataobject.gitlab.GitlabProjectDO;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import io.choerodon.websocket.helper.EnvListener;

/**
 * Created by younger on 2018/4/9.
 */
@Service
public class DevopsEnvironmentServiceImpl implements DevopsEnvironmentService {

    private static final String MASTER = "master";
    private static final String README = "README.md";
    private static final String README_CONTENT =
            "# This is gitops env repository!";
    private static final String ENV = "ENV";
    private static final String PROJECT_OWNER = "role/project/default/project-owner";
    private static final String PROJECT_MEMBER = "role/project/default/project-member";
    private Pattern pattern = Pattern.compile("^[-\\+]?[\\d]*$");
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
    private EnvListener envListener;
    @Autowired
    private DevopsServiceRepository devopsServiceRepository;
    @Autowired
    private ApplicationInstanceRepository applicationInstanceRepository;
    @Autowired
    private DevopsEnvironmentValidator devopsEnvironmentValidator;
    @Autowired
    private EnvUtil envUtil;
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
    private GitlabProjectRepository gitlabProjectRepository;

    @Override
    @Saga(code = "devops-create-env", description = "创建环境", inputSchema = "{}")
    public String create(Long projectId, DevopsEnviromentDTO devopsEnviromentDTO) {
        DevopsEnvironmentE devopsEnvironmentE = ConvertHelper.convert(devopsEnviromentDTO, DevopsEnvironmentE.class);
        devopsEnvironmentE.initProjectE(projectId);
        devopsEnviromentRepository.checkCode(devopsEnvironmentE);
        devopsEnviromentRepository.checkName(devopsEnvironmentE);
        devopsEnvironmentE.initActive(true);
        devopsEnvironmentE.initConnect(false);
        devopsEnvironmentE.initToken(GenerateUUID.generateUUID());
        devopsEnvironmentE.initProjectE(projectId);
        ProjectE projectE = iamRepository.queryIamProject(projectId);
        Organization organization = iamRepository.queryOrganizationById(projectE.getOrganization().getId());
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
        String repoUrl = String.format("git@%s:%s-%s-gitops/%s.git",
                gitlabSshUrl, organization.getCode(), projectE.getCode(), devopsEnvironmentE.getCode());
        InputStream inputStream = this.getClass().getResourceAsStream("/shell/environment.sh");
        Map<String, String> params = new HashMap<>();
        params.put("{NAMESPACE}", devopsEnvironmentE.getCode());
        params.put("{VERSION}", agentExpectVersion);
        params.put("{SERVICEURL}", agentServiceUrl);
        params.put("{TOKEN}", devopsEnvironmentE.getToken());
        params.put("{REPOURL}", agentRepoUrl);
        params.put("{ENVID}", devopsEnviromentRepository.create(devopsEnvironmentE)
                .getId().toString());
        params.put("{RSA}", sshKeys.get(0));
        params.put("{GITREPOURL}", repoUrl);
        GitlabGroupE gitlabGroupE = devopsProjectRepository.queryDevopsProject(projectId);
        UserAttrE userAttrE = userAttrRepository.queryById(TypeUtil.objToLong(GitUserNameUtil.getUserId()));
        GitlabProjectPayload gitlabProjectPayload = new GitlabProjectPayload();
        gitlabProjectPayload.setGroupId(TypeUtil.objToInteger(gitlabGroupE.getDevopsEnvGroupId()));
        gitlabProjectPayload.setUserId(TypeUtil.objToInteger(userAttrE.getGitlabUserId()));
        gitlabProjectPayload.setPath(devopsEnviromentDTO.getCode());
        gitlabProjectPayload.setOrganizationId(null);
        gitlabProjectPayload.setType(ENV);
        UserE userE = iamRepository.queryById(userAttrE.getIamUserId());
        gitlabProjectPayload.setLoginName(userE.getLoginName());
        gitlabProjectPayload.setRealName(userE.getRealName());

        // 创建环境时将项目下所有用户装入payload以便于saga消费
        gitlabProjectPayload.setUserIds(devopsEnviromentDTO.getUserIds());

        String input;
        try {
            input = objectMapper.writeValueAsString(gitlabProjectPayload);
            sagaClient.startSaga("devops-create-env", new StartInstanceDTO(input, "", ""));
            return FileUtil.replaceReturnString(inputStream, params);
        } catch (JsonProcessingException e) {
            throw new CommonException(e.getMessage(), e);
        }
    }


    @Override
    public List<DevopsEnvGroupEnvsDTO> listDevopsEnvGroupEnvs(Long projectId, Boolean active) {
        List<DevopsEnvGroupEnvsDTO> devopsEnvGroupEnvsDTOS = new ArrayList<>();
        List<DevopsEnviromentRepDTO> devopsEnviromentRepDTOS = listByProjectIdAndActive(projectId, active);
        if (!active) {
            DevopsEnvGroupEnvsDTO devopsEnvGroupEnvsDTO = new DevopsEnvGroupEnvsDTO();
            devopsEnvGroupEnvsDTO.setDevopsEnviromentRepDTOs(devopsEnviromentRepDTOS);
            devopsEnvGroupEnvsDTOS.add(devopsEnvGroupEnvsDTO);
            return devopsEnvGroupEnvsDTOS;
        }
        List<DevopsEnvGroupE> devopsEnvGroupES = devopsEnvGroupRepository.listByProjectId(projectId);
        devopsEnviromentRepDTOS.forEach(devopsEnviromentRepDTO -> {
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
        if (active) {
            devopsEnvGroupES.forEach(devopsEnvGroupE -> {
                if (!envGroupIds.contains(devopsEnvGroupE.getId())) {
                    DevopsEnvGroupEnvsDTO devopsEnvGroupEnvsDTO = new DevopsEnvGroupEnvsDTO();
                    devopsEnvGroupEnvsDTO.setDevopsEnvGroupId(devopsEnvGroupE.getId());
                    devopsEnvGroupEnvsDTO.setDevopsEnvGroupName(devopsEnvGroupE.getName());
                    devopsEnvGroupEnvsDTOS.add(devopsEnvGroupEnvsDTO);
                }
            });
        }
        return devopsEnvGroupEnvsDTOS;
    }

    @Override
    public List<DevopsEnviromentRepDTO> listByProjectIdAndActive(Long projectId, Boolean active) {

        //查询当前用户的环境权限
        List<Long> permissionEnvIds = devopsEnvUserPermissionRepository.listByUserId(TypeUtil.objToLong(GitUserNameUtil.getUserId())).stream().filter(devopsEnvUserPermissionE -> devopsEnvUserPermissionE.getPermitted() == true).map(DevopsEnvUserPermissionE::getEnvId).collect(Collectors.toList());
        ProjectE projectE = iamRepository.queryIamProject(projectId);
        //查询当前用户是否为项目所有者
        Boolean isProjectOwner = devopsEnvUserPermissionRepository.isProjectOwner(TypeUtil.objToLong(GitUserNameUtil.getUserId()), projectE);

        List<Long> connectedEnvList = envUtil.getConnectedEnvList(envListener);
        List<Long> updatedEnvList = envUtil.getUpdatedEnvList(envListener);
        List<DevopsEnvironmentE> devopsEnvironmentES = devopsEnviromentRepository
                .queryByprojectAndActive(projectId, active).stream().peek(t -> {
                    t.setUpdate(false);
                    setEnvStatus(connectedEnvList, updatedEnvList, t);
                    //项目成员返回拥有对应权限的环境，项目所有者返回所有环境
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
        if (!active) {
            devopsEnvironmentValidator.checkEnvCanDisabled(environmentId);
        }
        List<DevopsEnvironmentE> devopsEnvironmentES = devopsEnviromentRepository
                .queryByprojectAndActive(projectId, true);
        DevopsEnvironmentE devopsEnvironmentE = devopsEnviromentRepository.queryById(environmentId);
        devopsEnvironmentE.setActive(active);
        //启用环境，原环境不在环境组内，则序列在默认组内环境递增，员环境在环境组内，则序列在环境组内环境递增
        if (active) {
            if (devopsEnvironmentE.getDevopsEnvGroupId() == null) {
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
        devopsEnvironmentE.initProjectE(projectId);
        if (checkNameChange(devopsEnvironmentUpdateDTO)) {
            devopsEnviromentRepository.checkName(devopsEnvironmentE);
        }
        List<DevopsEnvironmentE> devopsEnvironmentES = devopsEnviromentRepository
                .queryByprojectAndActive(projectId, true);
        DevopsEnvironmentE beforeDevopsEnvironmentE = devopsEnviromentRepository
                .queryById(devopsEnvironmentUpdateDTO.getId());
        List<Long> ids;
        //更新环境，包含默认组到环境组，环境组到环境组，环境组到默认组,此时将初始组sequence重新排列,新环境在所选环境组中环境sequence递增
        if (devopsEnvironmentUpdateDTO.getDevopsEnvGroupId() != null) {
            if (beforeDevopsEnvironmentE.getDevopsEnvGroupId() == null) {
                ids = devopsEnvironmentES.stream().filter(devopsEnvironmentE1 ->
                        devopsEnvironmentE1.getDevopsEnvGroupId() == null)
                        .sorted(Comparator.comparing(DevopsEnvironmentE::getSequence)).map(
                                DevopsEnvironmentE::getId)
                        .collect(Collectors.toList());
            } else {
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
        } else {
            if (beforeDevopsEnvironmentE.getDevopsEnvGroupId() != null) {
                ids = devopsEnvironmentES.stream().filter(devopsEnvironmentE1 ->
                        beforeDevopsEnvironmentE.getDevopsEnvGroupId()
                                .equals(devopsEnvironmentE1.getDevopsEnvGroupId()))
                        .sorted(Comparator.comparing(DevopsEnvironmentE::getSequence)).map(
                                DevopsEnvironmentE::getId)
                        .collect(Collectors.toList());
                ids.remove(devopsEnvironmentUpdateDTO.getId());
                sort(ids.toArray(new Long[ids.size()]));
                devopsEnvironmentE.initSequence(devopsEnvironmentES.stream().filter(devopsEnvironmentE1 ->
                        devopsEnvironmentE1.getDevopsEnvGroupId() == null).collect(Collectors.toList()));
            }
        }
        return ConvertHelper.convert(devopsEnviromentRepository.update(
                devopsEnvironmentE), DevopsEnvironmentUpdateDTO.class);
    }

    @Override
    public DevopsEnvGroupEnvsDTO sort(Long[] environmentIds) {
        DevopsEnvGroupEnvsDTO devopsEnvGroupEnvsDTO = new DevopsEnvGroupEnvsDTO();
        List<Long> ids = new ArrayList<>();
        Collections.addAll(ids, environmentIds);
        List<DevopsEnvironmentE> devopsEnvironmentES = ids.stream()
                .map(id -> devopsEnviromentRepository.queryById(id))
                .collect(Collectors.toList());
        long sequence = 1L;
        for (DevopsEnvironmentE devopsEnvironmentE : devopsEnvironmentES) {
            devopsEnvironmentE.setSequence(sequence);
            devopsEnviromentRepository.update(devopsEnvironmentE);
            sequence = sequence + 1;
        }
        List<Long> connectedEnvList = envUtil.getConnectedEnvList(envListener);
        List<Long> updatedEnvList = envUtil.getUpdatedEnvList(envListener);
        devopsEnvironmentES.forEach(t -> {
            t.setUpdate(false);
            setEnvStatus(connectedEnvList, updatedEnvList, t);
        });
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

    private void setEnvStatus(List<Long> connectedEnvList, List<Long> updatedEnvList, DevopsEnvironmentE t) {
        if (connectedEnvList.contains(t.getId())) {
            if (updatedEnvList.contains(t.getId())) {
                t.initConnect(true);
            } else {
                t.setUpdate(true);
                t.initConnect(false);
                t.setUpdateMessage("Version is too low, please upgrade!");
            }
        } else {
            t.initConnect(false);
        }
    }

    @Override
    public String queryShell(Long environmentId, Boolean update) {
        if (update == null) {
            update = false;
        }
        DevopsEnvironmentE devopsEnvironmentE = devopsEnviromentRepository.queryById(environmentId);
        InputStream inputStream;
        Map<String, String> params = new HashMap<>();
        if (update) {
            inputStream = this.getClass().getResourceAsStream("/shell/environment-upgrade.sh");
        } else {
            inputStream = this.getClass().getResourceAsStream("/shell/environment.sh");
        }
        params.put("{NAMESPACE}", devopsEnvironmentE.getCode());
        params.put("{VERSION}", agentExpectVersion);
        params.put("{SERVICEURL}", agentServiceUrl);
        params.put("{TOKEN}", devopsEnvironmentE.getToken());
        params.put("{REPOURL}", agentRepoUrl);
        params.put("{ENVID}", devopsEnvironmentE.getId().toString());
        return FileUtil.replaceReturnString(inputStream, params);
    }

    @Override
    public void checkName(Long projectId, String name) {
        DevopsEnvironmentE devopsEnvironmentE = DevopsEnvironmentFactory.createDevopsEnvironmentE();
        devopsEnvironmentE.initProjectE(projectId);
        devopsEnvironmentE.setName(name);
        devopsEnviromentRepository.checkName(devopsEnvironmentE);
    }

    @Override
    public void checkCode(Long projectId, String code) {
        DevopsEnvironmentE devopsEnvironmentE = DevopsEnvironmentFactory.createDevopsEnvironmentE();
        devopsEnvironmentE.initProjectE(projectId);
        devopsEnvironmentE.setCode(code);
        devopsEnviromentRepository.checkCode(devopsEnvironmentE);
    }

    private Boolean checkNameChange(DevopsEnvironmentUpdateDTO devopsEnvironmentUpdateDTO) {
        DevopsEnvironmentE devopsEnvironmentE = devopsEnviromentRepository
                .queryById(devopsEnvironmentUpdateDTO.getId());
        return !devopsEnvironmentE.getName().equals(devopsEnvironmentUpdateDTO.getName());
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
        GitlabGroupE gitlabGroupE = devopsProjectRepository.queryByEnvGroupId(
                TypeUtil.objToInteger(gitlabProjectPayload.getGroupId()));
        DevopsEnvironmentE devopsEnvironmentE = devopsEnviromentRepository
                .queryByProjectIdAndCode(gitlabGroupE.getProjectE().getId(), gitlabProjectPayload.getPath());
        ProjectE projectE = iamRepository.queryIamProject(gitlabGroupE.getProjectE().getId());
        Organization organization = iamRepository.queryOrganizationById(projectE.getOrganization().getId());

        // 创建环境时初始化用户权限，分为gitlab权限和devops环境用户表权限
        initUserPermissionWhenCreatingEnv(gitlabProjectPayload, devopsEnvironmentE.getId(), projectE.getId());
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
        if (projectHooks == null) {
            devopsEnvironmentE.initHookId(TypeUtil.objToLong(gitlabRepository.createWebHook(
                    gitlabProjectDO.getId(), gitlabProjectPayload.getUserId(), projectHook).getId()));
        } else {
            devopsEnvironmentE.initHookId(TypeUtil.objToLong(projectHooks.get(0).getId()));
        }
        if (!gitlabRepository.getFile(gitlabProjectDO.getId(), MASTER, README)) {
            gitlabRepository.createFile(gitlabProjectDO.getId(),
                    README, README_CONTENT, "ADD README", gitlabProjectPayload.getUserId());
        }
        devopsEnviromentRepository.update(devopsEnvironmentE);
    }

    private void initUserPermissionWhenCreatingEnv(GitlabProjectPayload gitlabProjectPayload, Long envId,
                                                   Long projectId) {

        List<Long> userIds = gitlabProjectPayload.getUserIds();
        // 获取项目下所有角色和角色的用户数量
        RoleAssignmentSearchDTO roleAssignmentSearchDTO = new RoleAssignmentSearchDTO();
        List<RoleDTO> roleDTOList = iamRepository
                .listRolesWithUserCountOnProjectLevel(projectId, roleAssignmentSearchDTO);
        // 获取项目成员的roleId
        Long projectMemberId = 0L;
        // 获取项目成员的数量
        Integer projectMemberCount = 0;
        for (RoleDTO roleDTO : roleDTOList) {
            if (PROJECT_MEMBER.equals(roleDTO.getCode())) {
                projectMemberId = roleDTO.getId();
                projectMemberCount = roleDTO.getUserCount();
            }
        }
        if (projectMemberId == 0) {
            throw new CommonException("error.get.member.roleId");
        }
        // 根据项目成员id查询项目下所有的项目成员
        Page<UserDTO> allProjectMemberPage = iamRepository
                .pagingQueryUsersByRoleIdOnProjectLevel(new PageRequest(0, projectMemberCount), roleAssignmentSearchDTO,
                        projectMemberId, projectId);

        // 所有项目成员中没权限的
        allProjectMemberPage.getContent().stream().filter(e -> !userIds.contains(e.getId())).forEach(e -> {
            Long userId = e.getId();
            String loginName = e.getLoginName();
            String realName = e.getRealName();
            UserAttrE userAttrE = userAttrRepository.queryById(userId);
            Long gitlabUserId = userAttrE.getGitlabUserId();
            updateGitlabProjectMember(envId, gitlabUserId, 0);
            devopsEnvUserPermissionRepository
                    .create(new DevopsEnvUserPermissionE(loginName, userId, realName, envId, false));
        });
        // 所有项目成员中有权限的
        allProjectMemberPage.getContent().stream().filter(e -> userIds.contains(e.getId())).forEach(e -> {
            Long userId = e.getId();
            String loginName = e.getLoginName();
            String realName = e.getRealName();
            UserAttrE userAttrE = userAttrRepository.queryById(userId);
            Long gitlabUserId = userAttrE.getGitlabUserId();
            updateGitlabProjectMember(envId, gitlabUserId, 40);
            devopsEnvUserPermissionRepository
                    .create(new DevopsEnvUserPermissionE(loginName, userId, realName, envId, true));
        });
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
    public String handDevopsEnvGitRepository(DevopsEnvironmentE devopsEnvironmentE) {
        ProjectE projectE = iamRepository.queryIamProject(devopsEnvironmentE.getProjectE().getId());
        Organization organization = iamRepository.queryOrganizationById(projectE.getOrganization().getId());
        //本地路径
        String path = String.format("gitops/%s/%s/%s",
                organization.getCode(), projectE.getCode(), devopsEnvironmentE.getCode());
        //生成环境git仓库ssh地址
        String url = GitUtil.getGitlabSshUrl(pattern, gitlabSshUrl, organization.getCode(),
                projectE.getCode(), devopsEnvironmentE.getCode());

        File file = new File(path);
        GitUtil gitUtil = new GitUtil(devopsEnvironmentE.getEnvIdRsa());
        if (!file.exists()) {
            gitUtil.cloneBySsh(path, url);
        }
        return path;
    }

    @Override
    public Page<DevopsEnvUserPermissionDTO> listUserPermissionByEnvId(Long projectId, PageRequest pageRequest,
                                                                      String searchParams, String envId) {
        if ("null".equals(envId)) {
            // 项目层查询角色列表以及该角色下的用户数量
            RoleAssignmentSearchDTO roleAssignmentSearchDTO = new RoleAssignmentSearchDTO();
            List<RoleDTO> roleDTOList = iamRepository
                    .listRolesWithUserCountOnProjectLevel(projectId, roleAssignmentSearchDTO);
            // 获取项目成员的roleId
            Long projectMemberId = 0L;
            for (RoleDTO roleDTO : roleDTOList) {
                if (PROJECT_MEMBER.equals(roleDTO.getCode())) {
                    projectMemberId = roleDTO.getId();
                }
            }
            if (projectMemberId == 0) {
                throw new CommonException("error.get.member.roleId");
            }
            // 根据项目成员id查询项目下所有的项目成员
            Page<UserDTO> allProjectMemberPage = iamRepository
                    .pagingQueryUsersByRoleIdOnProjectLevel(pageRequest, roleAssignmentSearchDTO, projectMemberId,
                            projectId);
            List<DevopsEnvUserPermissionDTO> allProjectMemberList = new ArrayList<>();

            Page<DevopsEnvUserPermissionDTO> devopsEnvUserPermissionDTOPage = new Page<>();
            allProjectMemberPage.getContent().forEach(e -> {
                DevopsEnvUserPermissionDTO devopsEnvUserPermissionDTO = new DevopsEnvUserPermissionDTO();
                devopsEnvUserPermissionDTO.setIamUserId(e.getId());
                devopsEnvUserPermissionDTO.setLoginName(e.getLoginName());
                devopsEnvUserPermissionDTO.setRealName(e.getRealName());
                devopsEnvUserPermissionDTO.setPermitted(false);
                allProjectMemberList.add(devopsEnvUserPermissionDTO);
            });
            BeanUtils.copyProperties(allProjectMemberPage, devopsEnvUserPermissionDTOPage);
            devopsEnvUserPermissionDTOPage.setContent(allProjectMemberList);
            return devopsEnvUserPermissionDTOPage;
        } else {
            // 普通的分页查询
            return devopsEnvUserPermissionRepository
                    .pageUserPermissionByOption(TypeUtil.objToLong(envId), pageRequest, searchParams);
        }
    }

    @Override
    public List<DevopsEnvUserPermissionDTO> listAllUserPermission(Long envId) {
        return devopsEnvUserPermissionRepository.listALlUserPermission(envId);
    }

    @Override
    public Integer updateEnvUserPermission(Long projectId, Long envId, List<Long> userIds) {
        List<DevopsEnvUserPermissionE> allUserList = devopsEnvUserPermissionRepository.listAll(envId);
        allUserList.forEach(e -> {
            Integer permissionNumber = e.getPermitted() ? 40 : 0;
            UserAttrE userAttrE = userAttrRepository.queryById(e.getIamUserId());
            Long gitlabUserId = userAttrE.getGitlabUserId();
            updateGitlabProjectMember(envId, gitlabUserId, permissionNumber);
        });
        return devopsEnvUserPermissionRepository.updateEnvUserPermission(envId, userIds);
    }

    private void updateGitlabProjectMember(Long envId, Long userId, Integer permission) {
        Long gitlabProjectId = devopsEnviromentRepository.queryById(envId).getGitlabEnvProjectId();
        if (permission == 0) {
            // permission为0的先查看在gitlab那边有没有权限，如果有，则删除gitlab权限
            GitlabGroupMemberE gitlabGroupMemberE = gitlabProjectRepository.getProjectMember(TypeUtil.objToInteger(gitlabProjectId), TypeUtil.objToInteger(userId));
            if (gitlabGroupMemberE != null) {
                gitlabRepository
                        .removeMemberFromProject(TypeUtil.objToInteger(gitlabProjectId), TypeUtil.objToInteger(userId));
            }
        } else {
            MemberDTO memberDTO = new MemberDTO();
            memberDTO.setUserId(TypeUtil.objToInteger(userId));
            memberDTO.setAccessLevel(permission);
            memberDTO.setExpiresAt("");
            gitlabRepository.addMemberIntoProject(TypeUtil.objToInteger(gitlabProjectId), memberDTO);
        }
    }

    private void setPermission(DevopsEnvironmentE devopsEnvironmentE, List<Long> permissionEnvIds, Boolean isProjectOwner) {
        if (permissionEnvIds.contains(devopsEnvironmentE.getId()) || isProjectOwner) {
            devopsEnvironmentE.setPermission(true);
        } else {
            devopsEnvironmentE.setPermission(false);
        }
    }

    @Override
    public void initMockService(SagaClient sagaClient) {
        this.sagaClient = sagaClient;
    }
}
