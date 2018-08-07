package io.choerodon.devops.app.service.impl;

import java.util.*;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import io.choerodon.core.convertor.ConvertHelper;
import io.choerodon.core.convertor.ConvertPageHelper;
import io.choerodon.core.domain.Page;
import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.dto.*;
import io.choerodon.devops.app.service.ApplicationInstanceService;
import io.choerodon.devops.app.service.DevopsEnvResourceService;
import io.choerodon.devops.domain.application.entity.*;
import io.choerodon.devops.domain.application.entity.gitlab.GitlabPipelineE;
import io.choerodon.devops.domain.application.entity.iam.UserE;
import io.choerodon.devops.domain.application.factory.ApplicationInstanceFactory;
import io.choerodon.devops.domain.application.handler.ObjectOperation;
import io.choerodon.devops.domain.application.repository.*;
import io.choerodon.devops.domain.application.valueobject.C7nHelmRelease;
import io.choerodon.devops.domain.application.valueobject.PipelineResultV;
import io.choerodon.devops.domain.application.valueobject.ReplaceResult;
import io.choerodon.devops.domain.service.DeployService;
import io.choerodon.devops.infra.common.util.*;
import io.choerodon.devops.infra.common.util.enums.*;
import io.choerodon.devops.infra.dataobject.ApplicationInstanceDO;
import io.choerodon.devops.infra.dataobject.ApplicationInstancesDO;
import io.choerodon.devops.infra.dataobject.ApplicationLatestVersionDO;
import io.choerodon.devops.infra.mapper.ApplicationInstanceMapper;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import io.choerodon.websocket.Msg;
import io.choerodon.websocket.helper.CommandSender;
import io.choerodon.websocket.helper.EnvListener;
import io.choerodon.websocket.helper.EnvSession;


/**
 * Created by Zenger on 2018/4/12.
 */
@Service
public class ApplicationInstanceServiceImpl implements ApplicationInstanceService {
    private static final String RELEASE_NAME = "ReleaseName";

    private static Gson gson = new Gson();
    @Autowired
    DevopsEnvFileResourceRepository devopsEnvFileResourceRepository;
    @Value("${agent.version}")
    private String agentExpectVersion;
    @Value("${services.helm.url}")
    private String helmUrl;
    @Autowired
    private ApplicationInstanceRepository applicationInstanceRepository;
    @Autowired
    private ApplicationVersionRepository applicationVersionRepository;
    @Autowired
    private ApplicationRepository applicationRepository;
    @Autowired
    private DevopsEnvironmentRepository devopsEnvironmentRepository;
    @Autowired
    private DeployService deployService;
    @Autowired
    private GitlabProjectRepository gitlabProjectRepository;
    @Autowired
    private IamRepository iamRepository;
    @Autowired
    private CommandSender commandSender;
    @Autowired
    private DevopsEnvCommandRepository devopsEnvCommandRepository;
    @Autowired
    private DevopsEnvCommandValueRepository devopsEnvCommandValueRepository;
    @Autowired
    private EnvListener envListener;
    @Autowired
    private EnvUtil envUtil;
    @Autowired
    private UserAttrRepository userAttrRepository;
    private ApplicationInstanceMapper applicationInstanceMapper;
    @Autowired
    private DevopsEnvPodRepository devopsEnvPodRepository;
    @Autowired
    private DevopsEnvResourceService devopsEnvResourceService;
    @Autowired
    private GitlabRepository gitlabRepository;

    @Override
    public Page<ApplicationInstanceDTO> listApplicationInstance(Long projectId, PageRequest pageRequest,
                                                                Long envId, Long versionId, Long appId, String params) {
        Map<String, EnvSession> envs = envListener.connectedEnv();
        Page<ApplicationInstanceE> applicationInstanceEPage = applicationInstanceRepository.listApplicationInstance(
                projectId, pageRequest, envId, versionId, appId, params);
        List<ApplicationInstanceE> applicationInstanceES = applicationInstanceEPage.getContent();
        setInstanceConnect(applicationInstanceES, envs);
        return ConvertPageHelper.convertPage(applicationInstanceEPage, ApplicationInstanceDTO.class);
    }

    @Override
    public List<ApplicationInstancesDTO> listApplicationInstances(Long projectId, Long appId) {
        List<ApplicationInstancesDO> instancesDOS = applicationInstanceRepository.getDeployInstances(projectId, appId);
        List<ApplicationLatestVersionDO> appLatestVersionList =
                applicationVersionRepository.listAppLatestVersion(projectId);
        Map<Long, ApplicationLatestVersionDO> latestVersionList = appLatestVersionList.stream()
                .collect(Collectors.toMap(ApplicationLatestVersionDO::getAppId, t -> t, (a, b) -> b));
        Map<Long, Integer> appInstancesListMap = new HashMap<>();
        List<ApplicationInstancesDTO> appInstancesList = new ArrayList<>();
        instancesDOS.forEach(t -> {
            ApplicationInstancesDTO instancesDTO = new ApplicationInstancesDTO();
            if (appInstancesListMap.get(t.getAppId()) == null) {
                if (t.getInstanceId() != null
                        || t.getVersionId().equals(latestVersionList.get(t.getAppId()).getVersionId())) {
                    instancesDTO = new ApplicationInstancesDTO(
                            t.getAppId(),
                            t.getPublishLevel(),
                            t.getAppName(),
                            t.getAppCode(),
                            latestVersionList.get(t.getAppId()).getVersionId(),
                            latestVersionList.get(t.getAppId()).getVersion());
                    instancesDTO.setProjectId(t.getProjectId());
                    if (t.getInstanceId() != null) {
                        addAppInstance(instancesDTO, t);
                    }
                    appInstancesListMap.put(t.getAppId(), appInstancesList.size());
                    appInstancesList.add(instancesDTO);
                }
            } else {
                instancesDTO = appInstancesList.get(appInstancesListMap.get(t.getAppId()));
                addInstanceIfNotExist(instancesDTO, t);
            }
            if (t.getInstanceId() != null
                    && t.getVersion().equalsIgnoreCase(instancesDTO.getLatestVersion())) {
                instancesDTO.addLatestVersionRunning();
            }
        });
        return appInstancesList;
    }

    private void addAppInstance(ApplicationInstancesDTO instancesDTO, ApplicationInstancesDO instancesDO) {
        EnvVersionDTO envVersionDTO = new EnvVersionDTO(
                instancesDO.getVersionId(),
                instancesDO.getVersion(),
                instancesDO.getInstanceId(),
                instancesDO.getInstanceCode(),
                instancesDO.getInstanceStatus());
        EnvInstanceDTO envInstanceDTO = new EnvInstanceDTO(instancesDO.getEnvId());
        envInstanceDTO.addEnvVersionDTOS(envVersionDTO);
        instancesDTO.appendEnvInstanceDTOS(envInstanceDTO);
        if (instancesDTO.getLatestVersionId().equals(instancesDO.getVersionId())) {
            instancesDTO.appendInstances(new EnvInstancesDTO(
                    instancesDO.getInstanceId(), instancesDO.getInstanceCode(), instancesDO.getInstanceStatus()));
        }
    }

    private void addInstanceIfNotExist(ApplicationInstancesDTO instancesDTO,
                                       ApplicationInstancesDO instancesDO) {
        EnvInstanceDTO envInstanceDTO = instancesDTO.queryLastEnvInstanceDTO();
        if (instancesDTO.getLatestVersionId().equals(instancesDO.getVersionId())) {
            instancesDTO.appendInstances(new EnvInstancesDTO(
                    instancesDO.getInstanceId(), instancesDO.getInstanceCode(), instancesDO.getInstanceStatus()));
        }
        if (envInstanceDTO.getEnvId().equals(instancesDO.getEnvId())) {
            EnvVersionDTO envVersionDTO = envInstanceDTO.queryLastEnvVersionDTO();
            if (envVersionDTO.getVersion().equals(instancesDO.getVersion())) {
                envVersionDTO.appendInstanceList(
                        instancesDO.getInstanceId(),
                        instancesDO.getInstanceCode(),
                        instancesDO.getInstanceStatus());
            } else {
                envInstanceDTO.addEnvVersionDTOS(new EnvVersionDTO(
                        instancesDO.getVersionId(),
                        instancesDO.getVersion(),
                        instancesDO.getInstanceId(),
                        instancesDO.getInstanceCode(),
                        instancesDO.getInstanceStatus()));
            }
        } else {
            EnvVersionDTO envVersionDTO = new EnvVersionDTO(
                    instancesDO.getVersionId(),
                    instancesDO.getVersion(),
                    instancesDO.getInstanceId(),
                    instancesDO.getInstanceCode(),
                    instancesDO.getInstanceStatus());
            envInstanceDTO = new EnvInstanceDTO(instancesDO.getEnvId());
            envInstanceDTO.addEnvVersionDTOS(envVersionDTO);
            instancesDTO.appendEnvInstanceDTOS(envInstanceDTO);
        }
    }

    @Override
    public ReplaceResult queryValues(Long appId, Long envId, Long versionId) {
        ReplaceResult replaceResult = new ReplaceResult();
        String versionValue = FileUtil.jungeValueFormat(applicationVersionRepository.queryValue(versionId));
        try {
            FileUtil.jungeYamlFormat(versionValue);
        } catch (Exception e) {
            replaceResult.setYaml(versionValue);
            replaceResult.setErrorMsg(e.getMessage());
            replaceResult.setTotalLine(FileUtil.getFileTotalLine(replaceResult.getYaml()) + 1);
            replaceResult.setErrorLines(getErrorLine(e.getMessage()));
            return replaceResult;
        }
        String deployValue = FileUtil.jungeValueFormat(
                applicationInstanceRepository.queryValueByEnvIdAndAppId(envId, appId));
        replaceResult.setYaml(versionValue);
        if (deployValue != null) {
            replaceResult = FileUtil.replace(versionValue, deployValue);
        }
        replaceResult.setTotalLine(FileUtil.getFileTotalLine(replaceResult.getYaml()) + 1);
        return replaceResult;
    }


    @Override
    public ReplaceResult queryValue(Long instanceId) {
        ReplaceResult replaceResult;
        ApplicationInstanceE applicationInstanceE = applicationInstanceRepository.selectById(instanceId);
        String yaml = FileUtil.jungeValueFormat(applicationInstanceRepository.queryValueByEnvIdAndAppId(
                applicationInstanceE.getDevopsEnvironmentE().getId(), applicationInstanceE.getApplicationE().getId()));
        String versionValue = applicationVersionRepository
                .queryValue(applicationInstanceE.getApplicationVersionE().getId());
        replaceResult = FileUtil.replace(versionValue, yaml);
        replaceResult.setTotalLine(FileUtil.getFileTotalLine(yaml) + 1);
        return replaceResult;
    }

    @Override
    public List<ErrorLineDTO> formatValue(ReplaceResult replaceResult) {
        try {
            FileUtil.jungeYamlFormat(replaceResult.getYaml());
        } catch (Exception e) {
            return getErrorLine(e.getMessage());
        }
        return new ArrayList<>();
    }

    @Override
    public ReplaceResult previewValues(ReplaceResult previewReplaceResult, Long appVersionId) {
        String versionValue = applicationVersionRepository.queryValue(appVersionId);
        ReplaceResult replaceResult = FileUtil.replace(versionValue, previewReplaceResult.getYaml());
        replaceResult.setTotalLine(FileUtil.getFileTotalLine(replaceResult.getYaml()) + 1);
        return replaceResult;
    }

    @Override
    public DevopsEnvPreviewDTO listByEnv(Long projectId, Long envId, String params) {
        Map<String, Object> maps = gson.fromJson(params, new TypeToken<Map<String, Object>>() {
        }.getType());
        Map<String, EnvSession> envs = envListener.connectedEnv();
        Map<String, Object> searchParamMap = TypeUtil.cast(maps.get(TypeUtil.SEARCH_PARAM));
        String paramMap = TypeUtil.cast(maps.get(TypeUtil.PARAM));
        List<ApplicationInstanceDO> applicationInstancesDOS = applicationInstanceMapper
                .listApplicationInstance(projectId, envId, null, null, searchParamMap, paramMap);
        List<ApplicationInstanceE> applicationInstanceES = ConvertHelper
                .convertList(applicationInstancesDOS, ApplicationInstanceE.class);
        setInstanceConnect(applicationInstanceES, envs);
        Map<String, List<ApplicationInstanceE>> resultMaps = applicationInstanceES.parallelStream()
                .collect(Collectors.groupingBy(t -> t.getApplicationE().getName()));
        DevopsEnvPreviewDTO devopsEnvPreviewDTO = new DevopsEnvPreviewDTO();
        List<DevopsEnvPreviewAppDTO> devopsEnvPreviewAppDTOS = new ArrayList<>();
        resultMaps.forEach((key, value) -> {
            DevopsEnvPreviewAppDTO devopsEnvPreviewAppDTO = new DevopsEnvPreviewAppDTO();
            devopsEnvPreviewAppDTO.setAppName(key);
            List<ApplicationInstanceDTO> applicationInstanceDTOS = ConvertHelper
                    .convertList(value, ApplicationInstanceDTO.class);
            devopsEnvPreviewAppDTO.setApplicationInstanceDTOS(applicationInstanceDTOS);
            devopsEnvPreviewAppDTOS.add(devopsEnvPreviewAppDTO);
        });
        devopsEnvPreviewDTO.setDevopsEnvPreviewAppDTOS(devopsEnvPreviewAppDTOS);
        return devopsEnvPreviewDTO;
    }

    @Override
    public DevopsEnvPreviewInstanceDTO getDevopsEnvPreviewInstance(Long instanceId) {
        DevopsEnvPreviewInstanceDTO devopsEnvPreviewInstanceDTO = new DevopsEnvPreviewInstanceDTO();
        List<DevopsEnvPodDTO> devopsEnvPodDTOS = ConvertHelper
                .convertList(devopsEnvPodRepository.selectByInstanceId(instanceId), DevopsEnvPodDTO.class);
        DevopsEnvResourceDTO devopsEnvResourceDTO = devopsEnvResourceService.listResources(instanceId);
        devopsEnvPreviewInstanceDTO.setDevopsEnvPodDTOS(devopsEnvPodDTOS);
        devopsEnvPreviewInstanceDTO.setIngressDTOS(devopsEnvResourceDTO.getIngressDTOS());
        devopsEnvPreviewInstanceDTO.setServiceDTOS(devopsEnvResourceDTO.getServiceDTOS());
        return devopsEnvPreviewInstanceDTO;
    }


    @Override
    public ApplicationInstanceDTO create(ApplicationDeployDTO applicationDeployDTO, boolean gitops) {
        FileUtil.jungeYamlFormat(applicationDeployDTO.getValues());
        UserAttrE userAttrE = new UserAttrE();
        if (!gitops) {
            userAttrE = userAttrRepository.queryById(TypeUtil.objToLong(GitUserNameUtil.getUserId()));
        }
        envUtil.checkEnvConnection(applicationDeployDTO.getEnvironmentId(), envListener);
        ApplicationE applicationE = applicationRepository.query(applicationDeployDTO.getAppId());
        DevopsEnvironmentE devopsEnvironmentE =
                devopsEnvironmentRepository.queryById(applicationDeployDTO.getEnvironmentId());
        ApplicationVersionE applicationVersionE =
                applicationVersionRepository.query(applicationDeployDTO.getAppVerisonId());
        ApplicationInstanceE applicationInstanceE = ApplicationInstanceFactory.create();
        applicationInstanceE.initApplicationVersionEById(applicationDeployDTO.getAppVerisonId());
        applicationInstanceE.initApplicationEById(applicationDeployDTO.getAppId());
        applicationInstanceE.initDevopsEnvironmentEById(applicationDeployDTO.getEnvironmentId());
        applicationInstanceE.setStatus(InstanceStatus.OPERATIING.getStatus());
        DevopsEnvCommandE devopsEnvCommandE = new DevopsEnvCommandE();
        if (applicationDeployDTO.getType().equals("create")) {
            if (!gitops) {
                applicationInstanceE.setCode(
                        String.format("%s-%s", applicationE.getCode(), GenerateUUID.generateUUID().substring(0, 5)));
            } else {
                applicationInstanceE.setCode(applicationDeployDTO.getInstanceName());
            }
            applicationInstanceE.setId(applicationInstanceRepository.create(applicationInstanceE).getId());
        }
        if (applicationDeployDTO.getType().equals("update")) {
            ApplicationInstanceE newApplicationInstanceE = applicationInstanceRepository.selectById(
                    applicationDeployDTO.getAppInstanceId());
            applicationInstanceE.setCode(newApplicationInstanceE.getCode());
            applicationInstanceE.setId(applicationDeployDTO.getAppInstanceId());
            applicationInstanceRepository.update(applicationInstanceE);
        }
        devopsEnvCommandE.setCommandType(CommandType.SYNC.getType());
        devopsEnvCommandE.setObject(ObjectType.INSTANCE.getType());
        devopsEnvCommandE.setObjectId(applicationInstanceE.getId());
        devopsEnvCommandE.setStatus(CommandStatus.DOING.getStatus());
        DevopsEnvCommandValueE devopsEnvCommandValueE = new DevopsEnvCommandValueE();
        devopsEnvCommandValueE.setValue(FileUtil.getChangeYaml(
                applicationVersionRepository.queryValue(applicationDeployDTO.getAppVerisonId()),
                applicationDeployDTO.getValues()));
        devopsEnvCommandE.initDevopsEnvCommandValueE(
                devopsEnvCommandValueRepository.create(devopsEnvCommandValueE).getId());
        devopsEnvCommandRepository.create(devopsEnvCommandE);
        if (!gitops) {
            ObjectOperation<C7nHelmRelease> objectOperation = new ObjectOperation<>();
            objectOperation.setType(getC7NHelmRelease(
                    applicationInstanceE, applicationVersionE, applicationDeployDTO, applicationE));
            Integer projectId = TypeUtil.objToInteger(devopsEnvironmentE.getGitlabEnvProjectId());
            objectOperation.operationEnvGitlabFile(
                    applicationInstanceE.getCode(),
                    projectId,
                    applicationDeployDTO.getType(),
                    userAttrE.getGitlabUserId());
        }
        return ConvertHelper.convert(applicationInstanceE, ApplicationInstanceDTO.class);
    }

    @Override
    public List<VersionFeaturesDTO> queryVersionFeatures(Long appInstanceId) {
        ApplicationInstanceE applicationInstanceE = applicationInstanceRepository.selectById(appInstanceId);
        ApplicationE applicationE = applicationRepository.query(
                applicationInstanceE.getApplicationE().getId());

        Integer gitlabProjectId = applicationE.getGitlabProjectE().getId();
        List<GitlabPipelineE> gitlabPipelineEList =
                gitlabProjectRepository.listPipeline(gitlabProjectId, GitUserNameUtil.getUserId());
        if (gitlabPipelineEList == null) {
            return Collections.emptyList();
        }

        List<PipelineResultV> pipelineResultVS = new ArrayList<>();
        String branch = "";
        long pipelineId = 0;
        for (GitlabPipelineE gitlabPipeline : gitlabPipelineEList) {
            PipelineResultV pipelineResultV = new PipelineResultV();
            GitlabPipelineE gitlabPipelineE = gitlabProjectRepository.getPipeline(
                    gitlabProjectId, gitlabPipeline.getId(), GitUserNameUtil.getUserId());
            if (gitlabPipelineE != null) {
                pipelineResultV.setId(gitlabPipelineE.getId().longValue());
                pipelineResultV.setRef(gitlabPipelineE.getRef());

                UserE userE = iamRepository.queryByLoginName(gitlabPipelineE.getUser().getUsername());
                if (userE != null) {
                    pipelineResultV.setImageUrl(userE.getImageUrl());
                }
            }
            pipelineResultVS.add(pipelineResultV);
        }

        List<PipelineResultV> pipelineResultVList = new ArrayList<>();
        for (PipelineResultV pipelineResult : pipelineResultVS) {
            if (branch.equals(pipelineResult.getRef())
                    && pipelineId >= pipelineResult.getId()) {
                pipelineResultVList.add(pipelineResult);
            }
        }

        Collections.sort(pipelineResultVList);

        return ConvertHelper.convertList(pipelineResultVList, VersionFeaturesDTO.class);

    }

    @Override
    public List<AppInstanceCodeDTO> listByOptions(Long projectId, Long appId, Long appVersionId, Long envId) {
        return ConvertHelper.convertList(applicationInstanceRepository
                .listByOptions(projectId, appId, appVersionId, envId), AppInstanceCodeDTO.class);
    }

    @Override
    public void instanceStop(Long instanceId) {
        ApplicationInstanceE instanceE = applicationInstanceRepository.selectById(instanceId);
        envUtil.checkEnvConnection(instanceE.getDevopsEnvironmentE().getId(), envListener);
        if (!instanceE.getStatus().equals(InstanceStatus.RUNNING.getStatus())) {
            throw new CommonException("error.instance.notRunning");
        }
        DevopsEnvCommandE devopsEnvCommandE = devopsEnvCommandRepository
                .queryByObject(ObjectType.INSTANCE.getType(), instanceId);
        devopsEnvCommandE.setCommandType(CommandType.STOP.getType());
        devopsEnvCommandE.setStatus(CommandStatus.DOING.getStatus());
        devopsEnvCommandE.setId(null);
        devopsEnvCommandE = devopsEnvCommandRepository.create(devopsEnvCommandE);
        String namespace = getNameSpace(instanceE.getDevopsEnvironmentE().getId());
        String releaseName = updateInstanceStatus(instanceId, InstanceStatus.OPERATIING.getStatus());
        Map<String, String> stopMap = new HashMap<>();
        stopMap.put(RELEASE_NAME, releaseName);
        String payload = gson.toJson(stopMap);
        Long envId = instanceE.getDevopsEnvironmentE().getId();
        sentInstance(payload, releaseName, HelmType.HELM_RELEASE_STOP.toValue(),
                namespace, devopsEnvCommandE.getId(), envId);
    }

    @Override
    public void instanceStart(Long instanceId) {
        ApplicationInstanceE instanceE = applicationInstanceRepository.selectById(instanceId);
        envUtil.checkEnvConnection(instanceE.getDevopsEnvironmentE().getId(), envListener);
        if (!instanceE.getStatus().equals(InstanceStatus.STOPED.getStatus())) {
            throw new CommonException("error.instance.notStop");
        }
        DevopsEnvCommandE devopsEnvCommandE = devopsEnvCommandRepository
                .queryByObject(ObjectType.INSTANCE.getType(), instanceId);
        devopsEnvCommandE.setCommandType(CommandType.RESTART.getType());
        devopsEnvCommandE.setStatus(CommandStatus.DOING.getStatus());
        devopsEnvCommandE.setId(null);
        devopsEnvCommandE = devopsEnvCommandRepository.create(devopsEnvCommandE);
        String namespace = getNameSpace(instanceE.getDevopsEnvironmentE().getId());
        String releaseName = updateInstanceStatus(instanceId, InstanceStatus.OPERATIING.getStatus());
        Map<String, String> stopMap = new HashMap<>();
        stopMap.put(RELEASE_NAME, releaseName);
        String payload = gson.toJson(stopMap);
        Long envId = instanceE.getDevopsEnvironmentE().getId();
        sentInstance(payload, releaseName, HelmType.HELM_RELEASE_START.toValue(),
                namespace, devopsEnvCommandE.getId(), envId);
    }

    @Override
    public void instanceDelete(Long instanceId, boolean gitops) {
        ApplicationInstanceE instanceE = applicationInstanceRepository.selectById(instanceId);
        UserAttrE userAttrE = new UserAttrE();
        if (!gitops) {
            userAttrE = userAttrRepository.queryById(TypeUtil.objToLong(GitUserNameUtil.getUserId()));
        }
        envUtil.checkEnvConnection(instanceE.getDevopsEnvironmentE().getId(), envListener);
        DevopsEnvCommandE devopsEnvCommandE = devopsEnvCommandRepository
                .queryByObject(ObjectType.INSTANCE.getType(), instanceId);
        devopsEnvCommandE.setCommandType(CommandType.DELETE.getType());
        devopsEnvCommandE.setStatus(CommandStatus.DOING.getStatus());
        devopsEnvCommandE.setId(null);
        devopsEnvCommandRepository.create(devopsEnvCommandE);
        updateInstanceStatus(instanceId, InstanceStatus.OPERATIING.getStatus());
        DevopsEnvironmentE devopsEnvironmentE = devopsEnvironmentRepository
                .queryById(instanceE.getDevopsEnvironmentE().getId());
        if (!gitops) {
            DevopsEnvFileResourceE devopsEnvFileResourceE = devopsEnvFileResourceRepository
                    .queryByEnvIdAndResource(devopsEnvironmentE.getId(), instanceId, "C7NHelmRelease");
            gitlabRepository.deleteFile(
                    TypeUtil.objToInteger(devopsEnvironmentE.getGitlabEnvProjectId()),
                    devopsEnvFileResourceE.getFilePath(),
                    "DELETE FILE",
                    TypeUtil.objToInteger(userAttrE.getGitlabUserId()));
        }
    }

    private String getNameSpace(Long envId) {
        return devopsEnvironmentRepository.queryById(envId).getCode();
    }

    private String updateInstanceStatus(Long instanceId, String status) {
        ApplicationInstanceE instanceE = applicationInstanceRepository.selectById(instanceId);
        instanceE.setStatus(status);
        applicationInstanceRepository.update(instanceE);
        return instanceE.getCode();
    }

    @Async
    void sentInstance(String payload, String name, String type, String namespace, Long commandId, Long envId) {
        Msg msg = new Msg();
        msg.setKey("env:" + namespace + ".envId:" + envId + ".release:" + name);
        msg.setType(type);
        msg.setPayload(payload);
        msg.setCommandId(commandId);
        commandSender.sendMsg(msg);
    }

    private List<ErrorLineDTO> getErrorLine(String value) {
        List<ErrorLineDTO> errorLines = new ArrayList<>();
        List<Long> lineNumbers = new ArrayList<>();
        String[] errorMsg = value.split("\\^");
        for (int i = 0; i < value.length(); i++) {
            int j;
            for (j = i; j < value.length(); j++) {
                if (value.substring(i, j).equals("line")) {
                    lineNumbers.add(TypeUtil.objToLong(value.substring(j, value.indexOf(',', j)).trim()));
                }
            }
        }
        for (int i = 0; i < lineNumbers.size(); i++) {
            ErrorLineDTO errorLineDTO = new ErrorLineDTO();
            errorLineDTO.setLineNumber(lineNumbers.get(i));
            errorLineDTO.setErrorMsg(errorMsg[i]);
            errorLines.add(errorLineDTO);
        }
        return errorLines;
    }

    private void setInstanceConnect(List<ApplicationInstanceE> applicationInstanceES,
                                    Map<String, EnvSession> envSessionMap) {
        applicationInstanceES.parallelStream().forEach(applicationInstanceE ->
                applicationInstanceE.setConnect(envSessionMap.entrySet().parallelStream()
                        .anyMatch(entry -> {
                            EnvSession envSession = entry.getValue();
                            return envSession.getEnvId().equals(applicationInstanceE.getDevopsEnvironmentE().getId())
                                    && agentExpectVersion.compareTo(envSession.getVersion()) < 1;
                        })));
    }


    private C7nHelmRelease getC7NHelmRelease(ApplicationInstanceE applicationInstanceE,
                                             ApplicationVersionE applicationVersionE,
                                             ApplicationDeployDTO applicationDeployDTO,
                                             ApplicationE applicationE) {
        C7nHelmRelease c7nHelmRelease = new C7nHelmRelease();
        c7nHelmRelease.getMetadata().setName(applicationInstanceE.getCode());
        c7nHelmRelease.getSpec().setRepoUrl(helmUrl + applicationVersionE.getRepository());
        c7nHelmRelease.getSpec().setChartName(applicationE.getCode());
        c7nHelmRelease.getSpec().setChartVersion(applicationVersionE.getVersion());
        c7nHelmRelease.getSpec().setValues(FileUtil.getChangeYaml(
                applicationVersionRepository.queryValue(applicationDeployDTO.getAppVerisonId()),
                applicationDeployDTO.getValues()));
        return c7nHelmRelease;

    }
}
