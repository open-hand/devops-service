package io.choerodon.devops.app.service.impl;

import java.io.File;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.choerodon.asgard.saga.annotation.Saga;
import io.choerodon.asgard.saga.dto.StartInstanceDTO;
import io.choerodon.asgard.saga.feign.SagaClient;
import io.choerodon.core.convertor.ConvertHelper;
import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.dto.*;
import io.choerodon.devops.api.validator.DevopsEnvironmentValidator;
import io.choerodon.devops.app.service.DevopsEnvironmentService;
import io.choerodon.devops.domain.application.entity.DevopsEnvGroupE;
import io.choerodon.devops.domain.application.entity.DevopsEnvironmentE;
import io.choerodon.devops.domain.application.entity.ProjectE;
import io.choerodon.devops.domain.application.entity.UserAttrE;
import io.choerodon.devops.domain.application.entity.gitlab.GitlabGroupE;
import io.choerodon.devops.domain.application.event.GitlabProjectPayload;
import io.choerodon.devops.domain.application.factory.DevopsEnvironmentFactory;
import io.choerodon.devops.domain.application.repository.*;
import io.choerodon.devops.domain.application.valueobject.Organization;
import io.choerodon.devops.domain.application.valueobject.ProjectHook;
import io.choerodon.devops.infra.common.util.*;
import io.choerodon.devops.infra.common.util.enums.InstanceStatus;
import io.choerodon.devops.infra.dataobject.gitlab.GitlabProjectDO;
import io.choerodon.websocket.helper.EnvListener;


/**
 * Created by younger on 2018/4/9.
 */
@Service
public class DevopsEnvironmentServiceImpl implements DevopsEnvironmentService {

    private static final String README = "README.md";
    private static final String README_CONTENT =
            "# This is gitops env repository!";


    private static final String ENV = "ENV";
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
    private DevopsGitRepository devopsGitRepository;
    @Autowired
    private DevopsEnvCommitRepository devopsEnvCommitRepository;
    @Autowired
    private DevopsEnvGroupRepository devopsEnvGroupRepository;

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
        if (devopsEnviromentDTO.getDevopsEnvGroupId() == null) {
            devopsEnvironmentE.initSequence(devopsEnvironmentES.stream().filter(devopsEnvironmentE1 -> devopsEnvironmentE1.getDevopsEnvGroupId() == null).collect(Collectors.toList()));
        } else {
            devopsEnvironmentE.initSequence(devopsEnvironmentES.stream().filter(devopsEnvironmentE1 -> devopsEnviromentDTO.getDevopsEnvGroupId().equals(devopsEnvironmentE1.getDevopsEnvGroupId())).collect(Collectors.toList()));
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
        gitlabProjectPayload.setGroupId(gitlabGroupE.getEnvGroupId());
        gitlabProjectPayload.setUserId(TypeUtil.objToInteger(userAttrE.getGitlabUserId()));
        gitlabProjectPayload.setPath(devopsEnviromentDTO.getCode());
        gitlabProjectPayload.setOrganizationId(null);
        gitlabProjectPayload.setType(ENV);
        String input = null;
        try {
            input = objectMapper.writeValueAsString(gitlabProjectPayload);
            sagaClient.startSaga("devops-create-env", new StartInstanceDTO(input, "", ""));
            return FileUtil.replaceReturnString(inputStream, params);
        } catch (JsonProcessingException e) {
            throw new CommonException(e.getMessage());
        }
    }


    @Override
    public List<DevopsEnvGroupEnvsDTO> listDevopsEnvGroupEnvs(Long projectId, Boolean active) {
        List<DevopsEnvGroupEnvsDTO> devopsEnvGroupEnvsDTOS = new ArrayList<>();
        List<DevopsEnviromentRepDTO> devopsEnviromentRepDTOS = listByProjectIdAndActive(projectId, active);
        devopsEnviromentRepDTOS.stream().forEach(devopsEnviromentRepDTO -> {
            if (devopsEnviromentRepDTO.getDevopsEnvGroupId() == null) {
                devopsEnviromentRepDTO.setDevopsEnvGroupId(0L);
            }
        });
        Map<Long, List<DevopsEnviromentRepDTO>> resultMaps = devopsEnviromentRepDTOS.stream()
                .collect(Collectors.groupingBy(t -> t.getDevopsEnvGroupId()));
        resultMaps.forEach((key, value) -> {
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
        return devopsEnvGroupEnvsDTOS;
    }

    @Autowired
    public List<DevopsEnviromentRepDTO> listByProjectIdAndActive(Long projectId, Boolean active) {
        List<Long> connectedEnvList = envUtil.getConnectedEnvList(envListener);
        List<Long> updatedEnvList = envUtil.getUpdatedEnvList(envListener);
        List<DevopsEnvironmentE> devopsEnvironmentES = devopsEnviromentRepository
                .queryByprojectAndActive(projectId, active).parallelStream()
                .peek(t -> {
                    t.setUpdate(false);
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
        DevopsEnvironmentE devopsEnvironmentE = devopsEnviromentRepository.queryById(environmentId);
        devopsEnvironmentE.setActive(active);
        if (active) {
            devopsEnvironmentE.initSequence(devopsEnviromentRepository
                    .queryByprojectAndActive(projectId, active));
        } else {
            List<DevopsEnvironmentE> devopsEnvironmentES = devopsEnviromentRepository
                    .queryByprojectAndActive(projectId, true).parallelStream()
                    .sorted(Comparator.comparing(DevopsEnvironmentE::getSequence))
                    .collect(Collectors.toList());
            List<Long> environmentIds = devopsEnvironmentES
                    .stream()
                    .map(devopsEnvironmentE1
                            -> devopsEnvironmentE1.getId().longValue())
                    .collect(Collectors.toList());
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
        if (devopsEnvironmentUpdateDTO.getDevopsEnvGroupId() != null) {
            DevopsEnvironmentE beforeDevopsEnvironmentE = devopsEnviromentRepository.queryById(devopsEnvironmentUpdateDTO.getId());
            List<Long> ids;
            if (beforeDevopsEnvironmentE.getDevopsEnvGroupId() == null) {
                ids = devopsEnvironmentES.stream().filter(devopsEnvironmentE1 -> devopsEnvironmentE1.getDevopsEnvGroupId() == null).sorted(Comparator.comparing(DevopsEnvironmentE::getSequence)).map(DevopsEnvironmentE::getId).collect(Collectors.toList());
            } else {
                ids = devopsEnvironmentES.stream().filter(devopsEnvironmentE1 -> beforeDevopsEnvironmentE.getDevopsEnvGroupId().equals(devopsEnvironmentE1.getDevopsEnvGroupId())).sorted(Comparator.comparing(DevopsEnvironmentE::getSequence)).map(DevopsEnvironmentE::getId).collect(Collectors.toList());
            }
            ids.remove(devopsEnvironmentUpdateDTO.getId());
            sort(ids.toArray(new Long[ids.size()]));
            devopsEnvironmentE.initSequence(devopsEnvironmentES.stream().filter(devopsEnvironmentE1 -> (devopsEnvironmentUpdateDTO.getDevopsEnvGroupId()).equals(devopsEnvironmentE1.getDevopsEnvGroupId())).collect(Collectors.toList()));
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
        Long sequence = 1L;
        for (DevopsEnvironmentE devopsEnvironmentE : devopsEnvironmentES) {
            devopsEnvironmentE.setSequence(sequence);
            devopsEnviromentRepository.update(devopsEnvironmentE);
            sequence = sequence + 1;
        }
        List<Long> connectedEnvList = envUtil.getConnectedEnvList(envListener);
        List<Long> updatedEnvList = envUtil.getUpdatedEnvList(envListener);
        devopsEnvironmentES.stream()
                .forEach(t -> {
                    t.setUpdate(false);
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
                });
        DevopsEnvGroupE devopsEnvGroupE = new DevopsEnvGroupE();
        if (devopsEnvironmentES.get(0).getDevopsEnvGroupId() != null) {
            devopsEnvGroupE = devopsEnvGroupRepository.query(devopsEnvironmentES.get(0).getDevopsEnvGroupId());
        }
        devopsEnvGroupEnvsDTO.setDevopsEnviromentRepDTOs(ConvertHelper.convertList(devopsEnvironmentES, DevopsEnviromentRepDTO.class));
        devopsEnvGroupEnvsDTO.setDevopsEnvGroupName(devopsEnvGroupE.getName());
        devopsEnvGroupEnvsDTO.setDevopsEnvGroupId(devopsEnvGroupE.getId());
        return devopsEnvGroupEnvsDTO;
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

    /**
     * 校验name是否改变
     *
     * @param devopsEnvironmentUpdateDTO 环境参数
     * @return boolean
     */
    public Boolean checkNameChange(DevopsEnvironmentUpdateDTO devopsEnvironmentUpdateDTO) {
        DevopsEnvironmentE devopsEnvironmentE = devopsEnviromentRepository
                .queryById(devopsEnvironmentUpdateDTO.getId());
        return !devopsEnvironmentE.getName().equals(devopsEnvironmentUpdateDTO.getName());
    }

    @Override
    public List<DevopsEnviromentRepDTO> listByProjectId(Long projectId, Long appId) {
        List<DevopsEnviromentRepDTO> devopsEnviromentRepDTOList = listByProjectIdAndActive(projectId, true);

        if (appId == null) {
            return devopsEnviromentRepDTOList.stream().filter(t ->
                    applicationInstanceRepository.selectByEnvId(t.getId()).parallelStream()
                            .anyMatch(applicationInstanceE ->
                                    applicationInstanceE.getStatus().equals(InstanceStatus.RUNNING.getStatus())))
                    .collect(Collectors.toList());
        } else {
            return devopsEnviromentRepDTOList.stream().filter(t ->
                    applicationInstanceRepository.selectByEnvId(t.getId()).parallelStream()
                            .anyMatch(applicationInstanceE ->
                                    applicationInstanceE.getStatus().equals(InstanceStatus.RUNNING.getStatus()) && applicationInstanceE.getApplicationE().getId().equals(appId)))
                    .collect(Collectors.toList());
        }
    }

    @Override
    public void handleCreateEnvSaga(GitlabProjectPayload gitlabProjectPayload) {
        GitlabProjectDO gitlabProjectDO = gitlabRepository.createProject(
                gitlabProjectPayload.getGroupId(),
                gitlabProjectPayload.getPath(),
                gitlabProjectPayload.getUserId(),
                false);
        GitlabGroupE gitlabGroupE = devopsProjectRepository.queryByEnvGroupId(
                TypeUtil.objToInteger(gitlabProjectPayload.getGroupId()));
        DevopsEnvironmentE devopsEnvironmentE = devopsEnviromentRepository
                .queryByProjectIdAndCode(gitlabGroupE.getProjectE().getId(), gitlabProjectPayload.getPath());
        devopsEnvironmentE.initGitlabEnvProjectId(TypeUtil.objToLong(gitlabProjectDO.getId()));
        gitlabRepository.createDeployKey(
                gitlabProjectDO.getId(),
                gitlabProjectPayload.getPath(),
                devopsEnvironmentE.getEnvIdRsaPub(),
                true,
                gitlabProjectPayload.getUserId());
        ProjectHook projectHook = ProjectHook.allHook();
        projectHook.setEnableSslVerification(true);
        projectHook.setProjectId(gitlabProjectDO.getId());
        projectHook.setToken(devopsEnvironmentE.getToken());
        String uri = !gatewayUrl.endsWith("/") ? gatewayUrl + "/" : gatewayUrl;
        uri += "devops/webhook/git_ops";
        projectHook.setUrl(uri);
        devopsEnvironmentE.initHookId(TypeUtil.objToLong(gitlabRepository.createWebHook(
                gitlabProjectDO.getId(), gitlabProjectPayload.getUserId(), projectHook).getId()));
        gitlabRepository.createFile(gitlabProjectDO.getId(),
                README, README_CONTENT, "ADD README", gitlabProjectPayload.getUserId());
        devopsEnviromentRepository.update(devopsEnvironmentE);
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
                    .query(devopsEnvironmentE.getDevopsSyncCommit()).getCommitSha());
        }
        if (devopsEnvironmentE.getGitCommit() != null) {
            envSyncStatusDTO.setGitCommit(devopsEnvCommitRepository
                    .query(devopsEnvironmentE.getGitCommit()).getCommitSha());
        }
        envSyncStatusDTO.setCommitUrl(String.format("%s/%s-%s-gitops/%s/commit/",
                gitlabUrl, organization.getCode(), projectE.getCode(), devopsEnvironmentE.getCode()));
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
        String url = String.format("git@%s:%s-%s-gitops/%s.git",
                gitlabSshUrl, organization.getCode(), projectE.getCode(), devopsEnvironmentE.getCode());
        File file = new File(path);
        GitUtil gitUtil = new GitUtil(devopsEnvironmentE.getEnvIdRsa());
        if (!file.exists()) {
            gitUtil.cloneBySsh(path, url);
        }
        return path;
    }
}
