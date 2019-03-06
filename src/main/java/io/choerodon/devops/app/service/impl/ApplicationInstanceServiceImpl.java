package io.choerodon.devops.app.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import io.choerodon.core.convertor.ConvertHelper;
import io.choerodon.core.convertor.ConvertPageHelper;
import io.choerodon.core.domain.Page;
import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.dto.AppInstanceCodeDTO;
import io.choerodon.devops.api.dto.AppInstanceCommandLogDTO;
import io.choerodon.devops.api.dto.ApplicationDeployDTO;
import io.choerodon.devops.api.dto.ApplicationInstanceDTO;
import io.choerodon.devops.api.dto.ApplicationInstancesDTO;
import io.choerodon.devops.api.dto.DeployAppDTO;
import io.choerodon.devops.api.dto.DeployAppDetail;
import io.choerodon.devops.api.dto.DeployDetailDTO;
import io.choerodon.devops.api.dto.DeployFrequencyDTO;
import io.choerodon.devops.api.dto.DeployTimeDTO;
import io.choerodon.devops.api.dto.DevopsEnvPodDTO;
import io.choerodon.devops.api.dto.DevopsEnvPreviewAppDTO;
import io.choerodon.devops.api.dto.DevopsEnvPreviewDTO;
import io.choerodon.devops.api.dto.DevopsEnvPreviewInstanceDTO;
import io.choerodon.devops.api.dto.DevopsEnvResourceDTO;
import io.choerodon.devops.api.dto.EnvInstanceDTO;
import io.choerodon.devops.api.dto.EnvInstancesDTO;
import io.choerodon.devops.api.dto.EnvVersionDTO;
import io.choerodon.devops.api.dto.ErrorLineDTO;
import io.choerodon.devops.api.dto.InstanceControllerDetailDTO;
import io.choerodon.devops.api.validator.AppInstanceValidator;
import io.choerodon.devops.app.service.ApplicationInstanceService;
import io.choerodon.devops.app.service.DevopsEnvResourceService;
import io.choerodon.devops.app.service.DevopsEnvironmentService;
import io.choerodon.devops.app.service.GitlabGroupMemberService;
import io.choerodon.devops.domain.application.entity.ApplicationE;
import io.choerodon.devops.domain.application.entity.ApplicationInstanceE;
import io.choerodon.devops.domain.application.entity.ApplicationVersionE;
import io.choerodon.devops.domain.application.entity.DevopsEnvCommandE;
import io.choerodon.devops.domain.application.entity.DevopsEnvCommandValueE;
import io.choerodon.devops.domain.application.entity.DevopsEnvFileResourceE;
import io.choerodon.devops.domain.application.entity.DevopsEnvUserPermissionE;
import io.choerodon.devops.domain.application.entity.DevopsEnvironmentE;
import io.choerodon.devops.domain.application.entity.ProjectE;
import io.choerodon.devops.domain.application.entity.UserAttrE;
import io.choerodon.devops.domain.application.entity.iam.UserE;
import io.choerodon.devops.domain.application.handler.CheckOptionsHandler;
import io.choerodon.devops.domain.application.handler.ObjectOperation;
import io.choerodon.devops.domain.application.repository.ApplicationInstanceRepository;
import io.choerodon.devops.domain.application.repository.ApplicationRepository;
import io.choerodon.devops.domain.application.repository.ApplicationVersionRepository;
import io.choerodon.devops.domain.application.repository.DevopsAutoDeployRepository;
import io.choerodon.devops.domain.application.repository.DevopsEnvCommandRepository;
import io.choerodon.devops.domain.application.repository.DevopsEnvCommandValueRepository;
import io.choerodon.devops.domain.application.repository.DevopsEnvFileResourceRepository;
import io.choerodon.devops.domain.application.repository.DevopsEnvPodRepository;
import io.choerodon.devops.domain.application.repository.DevopsEnvUserPermissionRepository;
import io.choerodon.devops.domain.application.repository.DevopsEnvironmentRepository;
import io.choerodon.devops.domain.application.repository.GitlabRepository;
import io.choerodon.devops.domain.application.repository.IamRepository;
import io.choerodon.devops.domain.application.repository.UserAttrRepository;
import io.choerodon.devops.domain.application.valueobject.C7nHelmRelease;
import io.choerodon.devops.domain.application.valueobject.Metadata;
import io.choerodon.devops.domain.application.valueobject.ReplaceResult;
import io.choerodon.devops.domain.service.DeployService;
import io.choerodon.devops.infra.common.util.EnvUtil;
import io.choerodon.devops.infra.common.util.FileUtil;
import io.choerodon.devops.infra.common.util.GenerateUUID;
import io.choerodon.devops.infra.common.util.GitUserNameUtil;
import io.choerodon.devops.infra.common.util.JsonYamlConversionUtil;
import io.choerodon.devops.infra.common.util.TypeUtil;
import io.choerodon.devops.infra.common.util.enums.CommandStatus;
import io.choerodon.devops.infra.common.util.enums.CommandType;
import io.choerodon.devops.infra.common.util.enums.HelmType;
import io.choerodon.devops.infra.common.util.enums.InstanceStatus;
import io.choerodon.devops.infra.common.util.enums.ObjectType;
import io.choerodon.devops.infra.common.util.enums.ResourceType;
import io.choerodon.devops.infra.dataobject.ApplicationInstanceDO;
import io.choerodon.devops.infra.dataobject.ApplicationInstancesDO;
import io.choerodon.devops.infra.dataobject.ApplicationLatestVersionDO;
import io.choerodon.devops.infra.dataobject.DeployDO;
import io.choerodon.devops.infra.mapper.ApplicationInstanceMapper;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import io.choerodon.websocket.Msg;
import io.choerodon.websocket.helper.CommandSender;
import io.choerodon.websocket.helper.EnvListener;
import io.choerodon.websocket.helper.EnvSession;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.core.io.InputStreamResource;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by Zenger on 2018/4/12.
 */
@Service
public class ApplicationInstanceServiceImpl implements ApplicationInstanceService {

    public static final String CREATE = "create";
    public static final String UPDATE = "update";
    private static final String YAML_SUFFIX = ".yaml";
    private static final String RELEASE_PREFIX = "release-";
    private static final String FILE_SEPARATOR = "file.separator";
    private static final String C7NHELM_RELEASE = "C7NHelmRelease";
    private static final String RELEASE_NAME = "ReleaseName";

    private static Gson gson = new Gson();

    @Value("${agent.version}")
    private String agentExpectVersion;
    @Value("${services.helm.url}")
    private String helmUrl;
    @Value("${services.gitlab.url}")
    private String gitlabUrl;
    @Value("${services.gitlab.sshUrl}")
    private String gitlabSshUrl;

    @Autowired
    private DevopsEnvFileResourceRepository devopsEnvFileResourceRepository;
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
    @Autowired
    private ApplicationInstanceMapper applicationInstanceMapper;
    @Autowired
    private DevopsEnvPodRepository devopsEnvPodRepository;
    @Autowired
    private DevopsEnvResourceService devopsEnvResourceService;
    @Autowired
    private GitlabRepository gitlabRepository;
    @Autowired
    private GitlabGroupMemberService gitlabGroupMemberService;
    @Autowired
    private DevopsEnvironmentService devopsEnvironmentService;
    @Autowired
    private DevopsEnvUserPermissionRepository devopsEnvUserPermissionRepository;
    @Autowired
    private CheckOptionsHandler checkOptionsHandler;
    @Autowired
    private DevopsAutoDeployRepository devopsAutoDeployRepository;

    @Override
    public Page<DevopsEnvPreviewInstanceDTO> listApplicationInstance(Long projectId, PageRequest pageRequest,
                                                                     Long envId, Long versionId, Long appId, String params) {
        Map<String, EnvSession> envs = envListener.connectedEnv();

        Page<ApplicationInstanceE> applicationInstanceEPage = applicationInstanceRepository.listApplicationInstance(
                projectId, pageRequest, envId, versionId, appId, params);

        List<ApplicationInstanceE> applicationInstanceES = applicationInstanceEPage.getContent();
        setInstanceConnect(applicationInstanceES, envs);

        Page<ApplicationInstanceDTO> applicationInstanceDTOS = ConvertPageHelper
                .convertPage(applicationInstanceEPage, ApplicationInstanceDTO.class);

        return convertPageFromApplicationInstance(applicationInstanceDTOS);
    }

    /**
     * convert page from ApplicationInstance
     *
     * @param applicationInstanceDTOS the page that contains the instances
     * @return the page converted
     */
    private Page<DevopsEnvPreviewInstanceDTO> convertPageFromApplicationInstance(Page<ApplicationInstanceDTO> applicationInstanceDTOS) {
        Page<DevopsEnvPreviewInstanceDTO> previewInstanceDTOPage = new Page<>();
        previewInstanceDTOPage.setContent(applicationInstanceDTOS.getContent().stream().map(this::fromInstanceToEnvPreview).collect(Collectors.toList()));
        previewInstanceDTOPage.setNumber(applicationInstanceDTOS.getNumber());
        previewInstanceDTOPage.setNumberOfElements(applicationInstanceDTOS.getNumberOfElements());
        previewInstanceDTOPage.setSize(applicationInstanceDTOS.getSize());
        previewInstanceDTOPage.setTotalElements(applicationInstanceDTOS.getTotalElements());
        previewInstanceDTOPage.setTotalPages(applicationInstanceDTOS.getTotalPages());

        return previewInstanceDTOPage;
    }

    @Override
    public List<ApplicationInstancesDTO> listApplicationInstances(Long projectId, Long appId) {

        List<Long> permissionEnvIds = devopsEnvUserPermissionRepository
                .listByUserId(TypeUtil.objToLong(GitUserNameUtil.getUserId())).stream()
                .filter(DevopsEnvUserPermissionE::getPermitted).map(DevopsEnvUserPermissionE::getEnvId)
                .collect(Collectors.toList());

        ProjectE projectE = iamRepository.queryIamProject(projectId);
        if (iamRepository.isProjectOwner(TypeUtil.objToLong(GitUserNameUtil.getUserId()), projectE)) {
            permissionEnvIds = devopsEnvironmentRepository.queryByProject(projectId).stream()
                    .map(DevopsEnvironmentE::getId).collect(Collectors.toList());
        }

        List<ApplicationInstancesDO> instancesDOS = applicationInstanceRepository.getDeployInstances(projectId, appId,
                permissionEnvIds);
        List<ApplicationLatestVersionDO> appLatestVersionList = applicationVersionRepository
                .listAppLatestVersion(projectId);
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
                        addAppInstance(instancesDTO, t, latestVersionList.get(t.getAppId()).getVersionId());
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

    private void addAppInstance(ApplicationInstancesDTO instancesDTO, ApplicationInstancesDO instancesDO,
                                Long latestVersionId) {
        EnvVersionDTO envVersionDTO = new EnvVersionDTO(
                instancesDO.getVersionId(),
                instancesDO.getVersion(),
                instancesDO.getInstanceId(),
                instancesDO.getInstanceCode(),
                instancesDO.getInstanceStatus());
        EnvInstanceDTO envInstanceDTO = new EnvInstanceDTO(instancesDO.getEnvId());
        if (instancesDO.getVersionId().equals(latestVersionId)) {
            envVersionDTO.setLatest(true);
        }
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
            if (instancesDO.getVersionId().equals(instancesDTO.getLatestVersionId())) {
                envVersionDTO.setLatest(true);
            }
            envInstanceDTO.addEnvVersionDTOS(envVersionDTO);
            instancesDTO.appendEnvInstanceDTOS(envInstanceDTO);
        }
    }

    @Override
    public ReplaceResult queryValues(Long appId, Long envId, Long versionId) {
        ReplaceResult replaceResult = new ReplaceResult();
        String versionValue = FileUtil.checkValueFormat(applicationVersionRepository.queryValue(versionId));
        try {
            FileUtil.checkYamlFormat(versionValue);
        } catch (Exception e) {
            replaceResult.setYaml(versionValue);
            replaceResult.setErrorMsg(e.getMessage());
            replaceResult.setTotalLine(FileUtil.getFileTotalLine(replaceResult.getYaml()));
            replaceResult.setErrorLines(getErrorLine(e.getMessage()));
            return replaceResult;
        }
        String deployValue = FileUtil.checkValueFormat(
                applicationInstanceRepository.queryValueByEnvIdAndAppId(envId, appId));
        replaceResult.setYaml(versionValue);
        if (deployValue != null) {
            replaceResult = getReplaceResult(versionValue, deployValue);
        }
        return replaceResult;
    }

    @Override
    public ReplaceResult queryUpgradeValue(Long instanceId, Long versionId) {
        String yaml = FileUtil.checkValueFormat(applicationInstanceRepository.queryValueByInstanceId(instanceId));
        String versionValue = applicationVersionRepository.queryValue(versionId);
        return getReplaceResult(versionValue, yaml);
    }

    @Override
    public DeployTimeDTO listDeployTime(Long projectId, Long envId, Long[] appIds, Date startTime, Date endTime) {
        if (appIds.length == 0) {
            return new DeployTimeDTO();
        }
        List<DeployDO> deployDOS = applicationInstanceRepository
                .listDeployTime(projectId, envId, appIds, startTime, endTime);
        DeployTimeDTO deployTimeDTO = new DeployTimeDTO();
        List<Date> creationDates = deployDOS.stream().map(DeployDO::getCreationDate).collect(Collectors.toList());
        creationDates = new ArrayList<>(new HashSet<>(creationDates)).stream().sorted(Comparator.naturalOrder())
                .collect(Collectors.toList());
        List<DeployAppDTO> deployAppDTOS = new ArrayList<>();
        Map<String, List<DeployDO>> resultMaps = deployDOS.stream()
                .collect(Collectors.groupingBy(DeployDO::getAppName));
        resultMaps.forEach((key, value) -> {
            DeployAppDTO deployAppDTO = new DeployAppDTO();
            List<DeployAppDetail> deployAppDetails = new ArrayList<>();
            deployAppDTO.setAppName(key);
            value.forEach(deployDO -> {
                DeployAppDetail deployAppDetail = new DeployAppDetail();
                deployAppDetail.setDeployDate(deployDO.getCreationDate());
                deployAppDetail.setDeployTime(
                        getDeployTime(deployDO.getLastUpdateDate().getTime() - deployDO.getCreationDate().getTime()));
                deployAppDetails.add(deployAppDetail);
            });
            deployAppDTO.setDeployAppDetails(deployAppDetails);
            deployAppDTOS.add(deployAppDTO);
        });
        deployTimeDTO.setCreationDates(creationDates);
        deployTimeDTO.setDeployAppDTOS(deployAppDTOS);
        return deployTimeDTO;
    }

    @Override
    public DeployFrequencyDTO listDeployFrequency(Long projectId, Long[] envIds, Long appId, Date startTime,
                                                  Date endTime) {
        if (envIds.length == 0) {
            return new DeployFrequencyDTO();
        }
        List<DeployDO> deployFrequencyDOS = applicationInstanceRepository
                .listDeployFrequency(projectId, envIds, appId, startTime, endTime);
        Map<String, List<DeployDO>> resultMaps = deployFrequencyDOS.stream()
                .collect(Collectors.groupingBy(t -> new java.sql.Date(t.getCreationDate().getTime()).toString()));
        List<Long> deployFrequencys = new LinkedList<>();
        List<Long> deploySuccessFrequency = new LinkedList<>();
        List<Long> deployFailFrequency = new LinkedList<>();
        List<String> creationDates = deployFrequencyDOS.stream()
                .map(deployDO -> new java.sql.Date(deployDO.getCreationDate().getTime()).toString())
                .collect(Collectors.toList());
        creationDates = new ArrayList<>(new HashSet<>(creationDates)).stream().sorted(Comparator.naturalOrder())
                .collect(Collectors.toList());
        creationDates.forEach(date -> {
            Long[] newDeployFrequencys = {0L};
            Long[] newDeploySuccessFrequency = {0L};
            Long[] newDeployFailFrequency = {0L};
            resultMaps.get(date).forEach(deployFrequencyDO -> {
                newDeployFrequencys[0] = newDeployFrequencys[0] + 1L;
                if (deployFrequencyDO.getStatus().equals(CommandStatus.SUCCESS.getStatus())) {
                    newDeploySuccessFrequency[0] = newDeploySuccessFrequency[0] + 1L;
                } else {
                    newDeployFailFrequency[0] = newDeployFailFrequency[0] + 1L;
                }
            });
            deployFrequencys.add(newDeployFrequencys[0]);
            deploySuccessFrequency.add(newDeploySuccessFrequency[0]);
            deployFailFrequency.add(newDeployFailFrequency[0]);
        });
        DeployFrequencyDTO deployFrequencyDTO = new DeployFrequencyDTO();
        deployFrequencyDTO.setCreationDates(creationDates);
        deployFrequencyDTO.setDeployFailFrequency(deployFailFrequency);
        deployFrequencyDTO.setDeploySuccessFrequency(deploySuccessFrequency);
        deployFrequencyDTO.setDeployFrequencys(deployFrequencys);
        return deployFrequencyDTO;
    }

    @Override
    public Page<DeployDetailDTO> pageDeployFrequencyDetail(Long projectId, PageRequest pageRequest, Long[] envIds,
                                                           Long appId, Date startTime, Date endTime) {
        if (envIds.length == 0) {
            return new Page<>();
        }
        Page<DeployDO> deployDOS = applicationInstanceRepository.pageDeployFrequencyDetail(projectId, pageRequest,
                envIds, appId, startTime, endTime);
        return getDeployDetailDTOS(deployDOS);
    }

    @Override
    public Page<DeployDetailDTO> pageDeployTimeDetail(Long projectId, PageRequest pageRequest, Long[] appIds,
                                                      Long envId,
                                                      Date startTime, Date endTime) {
        if (appIds.length == 0) {
            return new Page<>();
        }
        Page<DeployDO> deployDOS = applicationInstanceRepository.pageDeployTimeDetail(projectId, pageRequest, envId,
                appIds, startTime, endTime);
        return getDeployDetailDTOS(deployDOS);
    }

    @Override
    public void deployTestApp(ApplicationDeployDTO applicationDeployDTO) {
        String versionValue = applicationVersionRepository.queryValue(applicationDeployDTO.getAppVersionId());
        ApplicationE applicationE = applicationRepository.query(applicationDeployDTO.getAppId());
        ApplicationVersionE applicationVersionE = applicationVersionRepository.query(applicationDeployDTO.getAppVersionId());
        FileUtil.checkYamlFormat(applicationDeployDTO.getValues());
        String deployValue = getReplaceResult(versionValue,
                applicationDeployDTO.getValues()).getDeltaYaml().trim();
        deployService.deployTestApp(applicationE, applicationVersionE, applicationDeployDTO.getInstanceName(), applicationDeployDTO.getEnvironmentId(), deployValue);
    }

    @Override
    public InstanceControllerDetailDTO getInstanceResourceDetailJson(Long instanceId, String resourceName, ResourceType resourceType) {
        String message = getAndCheckResourceDetail(instanceId, resourceName, resourceType);

        try {
            return new InstanceControllerDetailDTO(instanceId, new ObjectMapper().readTree(message));
        } catch (IOException e) {
            throw new CommonException("error.instance.resource.json.read.failed", instanceId, message);
        }
    }

    @Override
    public InstanceControllerDetailDTO getInstanceResourceDetailYaml(Long instanceId, String resourceName, ResourceType resourceType) {
        String message = getAndCheckResourceDetail(instanceId, resourceName, resourceType);

        try {
            return new InstanceControllerDetailDTO(instanceId, JsonYamlConversionUtil.json2yaml(message));
        } catch (IOException e) {
            throw new CommonException(JsonYamlConversionUtil.ERROR_JSON_TO_YAML_FAILED, message);
        }
    }

    private String getAndCheckResourceDetail(Long instanceId, String resourceName, ResourceType resourceType) {
        String message = applicationInstanceRepository.getInstanceResourceDetailJson(instanceId, resourceName, resourceType);
        if (StringUtils.isEmpty(message)) {
            throw new CommonException("error.instance.resource.not.found", instanceId, resourceType.getType());
        }
        return message;
    }

    @Override
    public void getTestAppStatus(Map<Long, List<String>> testReleases) {
        deployService.getTestAppStatus(testReleases);
    }

    @Override
    public void operationPodCount(String deploymentName, Long envId, Long count) {
        //校验用户是否有环境的权限
        DevopsEnvironmentE devopsEnvironmentE = checkEnvPermission(envId);
        //不能减少到0
        if (count == 0) {
            return;
        }
        deployService.operatePodCount(deploymentName, devopsEnvironmentE.getCode(), devopsEnvironmentE.getClusterE().getId(), count);
    }

    @Override
    public Page<AppInstanceCommandLogDTO> listAppInstanceCommand(PageRequest pageRequest, Long appInstanceId, Date startTime, Date endTime) {
        Page<DevopsEnvCommandE> devopsEnvCommandES = devopsEnvCommandRepository.listByObject(pageRequest, ObjectType.INSTANCE.getType(), appInstanceId, startTime, endTime);
        Set<Long> userIds = new HashSet<>();
        devopsEnvCommandES.stream().forEach(devopsEnvCommandE ->
                userIds.add(devopsEnvCommandE.getCreatedBy())
        );
        List<UserE> userES = iamRepository.listUsersByIds(new ArrayList<>(userIds));
        Page<AppInstanceCommandLogDTO> pageAppInstanceCommandLogDTOS = new Page<>();
        List<AppInstanceCommandLogDTO> appInstanceCommandLogDTOS = new ArrayList<>();
        BeanUtils.copyProperties(devopsEnvCommandES, pageAppInstanceCommandLogDTOS);
        devopsEnvCommandES.stream().forEach(devopsEnvCommandE -> {
            AppInstanceCommandLogDTO appInstanceCommandLogDTO = new AppInstanceCommandLogDTO();
            appInstanceCommandLogDTO.setType(devopsEnvCommandE.getCommandType());
            userES.stream().filter(userE -> userE.getId().equals(devopsEnvCommandE.getCreatedBy())).forEach(userE -> {
                appInstanceCommandLogDTO.setUserImage(userE.getImageUrl());
                appInstanceCommandLogDTO.setLoginName(userE.getLoginName());
                appInstanceCommandLogDTO.setRealName(userE.getRealName());
            });
            appInstanceCommandLogDTO.setCreateTime(devopsEnvCommandE.getCreationDate());
            appInstanceCommandLogDTOS.add(appInstanceCommandLogDTO);
        });
        pageAppInstanceCommandLogDTOS.setContent(appInstanceCommandLogDTOS);
        return pageAppInstanceCommandLogDTOS;
    }

    private DevopsEnvironmentE checkEnvPermission(Long envId) {
        devopsEnvUserPermissionRepository.checkEnvDeployPermission(TypeUtil.objToLong(GitUserNameUtil.getUserId()),
                envId);
        DevopsEnvironmentE devopsEnvironmentE = devopsEnvironmentRepository
                .queryById(envId);
        envUtil.checkEnvConnection(devopsEnvironmentE.getClusterE().getId(), envListener);
        return devopsEnvironmentE;
    }


    private Page<DeployDetailDTO> getDeployDetailDTOS(Page<DeployDO> deployDOS) {
        Page<DeployDetailDTO> pageDeployDetailDTOS = new Page<>();
        List<DeployDetailDTO> deployDetailDTOS = new ArrayList<>();
        BeanUtils.copyProperties(deployDOS, pageDeployDetailDTOS);
        deployDOS.getContent().forEach(deployDO -> {
            DeployDetailDTO deployDetailDTO = new DeployDetailDTO();
            BeanUtils.copyProperties(deployDO, deployDetailDTO);
            deployDetailDTO.setDeployTime(
                    getDeployTime(deployDO.getLastUpdateDate().getTime() - deployDO.getCreationDate().getTime()));
            if (deployDO.getCreatedBy() != 0) {
                UserE userE = iamRepository.queryUserByUserId(deployDO.getCreatedBy());
                deployDetailDTO.setLastUpdatedName(userE.getRealName());
            }
            deployDetailDTOS.add(deployDetailDTO);
        });
        pageDeployDetailDTOS.setContent(deployDetailDTOS);
        return pageDeployDetailDTOS;
    }

    @Override
    public ReplaceResult queryValue(Long instanceId) {
        ApplicationInstanceE applicationInstanceE = applicationInstanceRepository.selectById(instanceId);
        DevopsEnvCommandE devopsEnvCommandE = devopsEnvCommandRepository.query(applicationInstanceE.getCommandId());
        String yaml = FileUtil.checkValueFormat(applicationInstanceRepository.queryValueByInstanceId(
                instanceId));
        String versionValue;
        //实例表新增commndId之前的实例查values用实例的版本
        if (devopsEnvCommandE == null) {
            versionValue = applicationVersionRepository.queryValue(applicationInstanceE.getApplicationVersionE().getId());
        } else {
            versionValue = applicationVersionRepository
                    .queryValue(devopsEnvCommandE.getObjectVersionId());
        }
        return getReplaceResult(versionValue, yaml);
    }

    @Override
    public List<ErrorLineDTO> formatValue(ReplaceResult replaceResult) {
        try {
            FileUtil.checkYamlFormat(replaceResult.getYaml());

            String fileName = GenerateUUID.generateUUID() + YAML_SUFFIX;
            String path = "deployfile";
            FileUtil.saveDataToFile(path, fileName, replaceResult.getYaml());
            //读入文件
            File file = new File(path + System.getProperty(FILE_SEPARATOR) + fileName);
            InputStreamResource inputStreamResource = new InputStreamResource(new FileInputStream(file));
            YamlPropertySourceLoader yamlPropertySourceLoader = new YamlPropertySourceLoader();
            try {
                yamlPropertySourceLoader.load("test", inputStreamResource, null);
            } catch (Exception e) {
                FileUtil.deleteFile(path + System.getProperty(FILE_SEPARATOR) + fileName);
                return getErrorLine(e.getMessage());
            }
            FileUtil.deleteFile(path + System.getProperty(FILE_SEPARATOR) + fileName);
        } catch (Exception e) {
            return getErrorLine(e.getMessage());
        }
        return new ArrayList<>();
    }

    @Override
    public ReplaceResult previewValues(ReplaceResult previewReplaceResult, Long appVersionId) {
        String versionValue = applicationVersionRepository.queryValue(appVersionId);
        try {
            FileUtil.checkYamlFormat(previewReplaceResult.getYaml());
        } catch (Exception e) {
            throw new CommonException(e.getMessage(), e);
        }
        ReplaceResult replaceResult = getReplaceResult(versionValue, previewReplaceResult.getYaml());
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
        Map<String, List<ApplicationInstanceE>> resultMaps = applicationInstanceES.stream()
                .collect(Collectors.groupingBy(t -> t.getApplicationE().getName()));
        DevopsEnvPreviewDTO devopsEnvPreviewDTO = new DevopsEnvPreviewDTO();
        List<DevopsEnvPreviewAppDTO> devopsEnvPreviewAppDTOS = new ArrayList<>();
        resultMaps.forEach((key, value) -> {
            DevopsEnvPreviewAppDTO devopsEnvPreviewAppDTO = new DevopsEnvPreviewAppDTO();
            devopsEnvPreviewAppDTO.setAppName(key);
            ApplicationE applicationE = applicationRepository.query(value.get(0).getApplicationE().getId());
            devopsEnvPreviewAppDTO.setAppCode(applicationE.getCode());
            devopsEnvPreviewAppDTO.setProjectId(applicationE.getProjectE().getId());
            List<ApplicationInstanceDTO> applicationInstanceDTOS = ConvertHelper
                    .convertList(value, ApplicationInstanceDTO.class);

            // set instances
            devopsEnvPreviewAppDTO.setApplicationInstanceDTOS(
                    applicationInstanceDTOS.stream()
                            .map(this::fromInstanceToEnvPreview)
                            .collect(Collectors.toList())
            );

            devopsEnvPreviewAppDTOS.add(devopsEnvPreviewAppDTO);
        });
        devopsEnvPreviewDTO.setDevopsEnvPreviewAppDTOS(devopsEnvPreviewAppDTOS);
        return devopsEnvPreviewDTO;
    }


    /**
     * create the detail as env preview from application instance
     *
     * @param applicationInstanceDTO the basic information for {@link DevopsEnvPreviewInstanceDTO}
     * @return devopsEnvPreviewInstanceDTO
     */
    private DevopsEnvPreviewInstanceDTO fromInstanceToEnvPreview(ApplicationInstanceDTO applicationInstanceDTO) {
        DevopsEnvPreviewInstanceDTO devopsEnvPreviewInstanceDTO = new DevopsEnvPreviewInstanceDTO();
        BeanUtils.copyProperties(applicationInstanceDTO, devopsEnvPreviewInstanceDTO);

        // 获取相关的pod
        List<DevopsEnvPodDTO> devopsEnvPodDTOS = ConvertHelper
                .convertList(devopsEnvPodRepository.selectByInstanceId(devopsEnvPreviewInstanceDTO.getId()),
                        DevopsEnvPodDTO.class);

        DevopsEnvResourceDTO devopsEnvResourceDTO = devopsEnvResourceService
                .listResourcesInHelmRelease(devopsEnvPreviewInstanceDTO.getId());

        // 关联其pod并设置deployment
        devopsEnvPreviewInstanceDTO.setDeploymentDTOS(devopsEnvResourceDTO.getDeploymentDTOS()
                .stream()
                .peek(deploymentDTO -> deploymentDTO.setDevopsEnvPodDTOS(filterPodsAssociated(devopsEnvPodDTOS, deploymentDTO.getName())))
                .collect(Collectors.toList())
        );

        // 关联其pod并设置daemonSet
        devopsEnvPreviewInstanceDTO.setDaemonSetDTOS(
                devopsEnvResourceDTO.getDaemonSetDTOS()
                        .stream()
                        .peek(daemonSetDTO -> daemonSetDTO.setDevopsEnvPodDTOS(
                                filterPodsAssociatedWithDaemonSet(devopsEnvPodDTOS, daemonSetDTO.getName())
                        ))
                        .collect(Collectors.toList())
        );

        // 关联其pod并设置statefulSet
        devopsEnvPreviewInstanceDTO.setStatefulSetDTOS(
                devopsEnvResourceDTO.getStatefulSetDTOS()
                        .stream()
                        .peek(statefulSetDTO -> statefulSetDTO.setDevopsEnvPodDTOS(
                                filterPodsAssociatedWithStatefulSet(devopsEnvPodDTOS, statefulSetDTO.getName()))
                        )
                        .collect(Collectors.toList())
        );

        // 设置pvc
        devopsEnvPreviewInstanceDTO.setPersistentVolumeClaimDTOS(devopsEnvResourceDTO.getPersistentVolumeClaimDTOS());

        // 设置ingress
        devopsEnvPreviewInstanceDTO.setIngressDTOS(devopsEnvResourceDTO.getIngressDTOS());

        // 设置service
        devopsEnvPreviewInstanceDTO.setServiceDTOS(devopsEnvResourceDTO.getServiceDTOS());

        return devopsEnvPreviewInstanceDTO;
    }

    /**
     * filter the pods that are associated with the deployment.
     *
     * @param devopsEnvPodDTOS the pods to be filtered
     * @param deploymentName   the name of deployment
     * @return the pods
     */
    private List<DevopsEnvPodDTO> filterPodsAssociated(List<DevopsEnvPodDTO> devopsEnvPodDTOS, String deploymentName) {
        return devopsEnvPodDTOS.stream().filter(devopsEnvPodDTO -> {
                    String podName = devopsEnvPodDTO.getName();
                    String controllerNameFromPod = podName.substring(0,
                            podName.lastIndexOf('-', podName.lastIndexOf('-') - 1));
                    return deploymentName.equals(controllerNameFromPod);
                }
        ).collect(Collectors.toList());
    }

    /**
     * filter the pods that are associated with the daemonSet.
     *
     * @param devopsEnvPodDTOS the pods to be filtered
     * @param daemonSetName    the name of daemonSet
     * @return the pods
     */
    private List<DevopsEnvPodDTO> filterPodsAssociatedWithDaemonSet(List<DevopsEnvPodDTO> devopsEnvPodDTOS, String daemonSetName) {
        return devopsEnvPodDTOS
                .stream()
                .filter(
                        devopsEnvPodDTO -> daemonSetName.equals(devopsEnvPodDTO.getName().substring(0, devopsEnvPodDTO.getName().lastIndexOf('-')))
                )
                .collect(Collectors.toList());
    }

    /**
     * filter the pods that are associated with the statefulSet.
     *
     * @param devopsEnvPodDTOS the pods to be filtered
     * @param statefulSetName  the name of statefulSet
     * @return the pods
     */
    private List<DevopsEnvPodDTO> filterPodsAssociatedWithStatefulSet(List<DevopsEnvPodDTO> devopsEnvPodDTOS, String statefulSetName) {
        // statefulSet名称逻辑和daemonSet一致
        return filterPodsAssociatedWithDaemonSet(devopsEnvPodDTOS, statefulSetName);
    }

    public ApplicationInstanceDTO createOrUpdate(ApplicationDeployDTO applicationDeployDTO) {

        //校验用户是否有环境的权限
        DevopsEnvironmentE devopsEnvironmentE = checkEnvPermission(applicationDeployDTO.getEnvironmentId());

        //校验values
        FileUtil.checkYamlFormat(applicationDeployDTO.getValues());

        ApplicationE applicationE = applicationRepository.query(applicationDeployDTO.getAppId());
        ApplicationVersionE applicationVersionE =
                applicationVersionRepository.query(applicationDeployDTO.getAppVersionId());

        //初始化ApplicationInstanceE,DevopsEnvCommandE,DevopsEnvCommandValueE
        ApplicationInstanceE applicationInstanceE = initApplicationInstanceE(applicationDeployDTO);
        DevopsEnvCommandE devopsEnvCommandE = initDevopsEnvCommandE(applicationDeployDTO);
        DevopsEnvCommandValueE devopsEnvCommandValueE = initDevopsEnvCommandValueE(applicationDeployDTO);

        //校验更新实例时values是否删除key
        if (!applicationDeployDTO.getIsNotChange() && applicationDeployDTO.getType().equals(UPDATE)) {
            ApplicationInstanceE oldapplicationInstanceE = applicationInstanceRepository.selectById(applicationDeployDTO.getAppInstanceId());
            DevopsEnvCommandE olddevopsEnvCommandE = devopsEnvCommandRepository.query(oldapplicationInstanceE.getCommandId());
            if (applicationDeployDTO.getAppVersionId().equals(olddevopsEnvCommandE.getObjectVersionId())) {
                String oldDeployValue = applicationInstanceRepository.queryValueByInstanceId(
                        applicationDeployDTO.getAppInstanceId());
                String newDeployValue = devopsEnvCommandValueE.getValue();
                if (oldDeployValue.equals(newDeployValue)) {
                    throw new CommonException("error.values.key.delete");
                }
            }
        }

        // 初始化自定义实例名
        String code;
        if (applicationDeployDTO.getType().equals(CREATE)) {
            if (applicationDeployDTO.getInstanceName() == null || applicationDeployDTO.getInstanceName().trim().equals("")) {
                code = String.format("%s-%s", applicationE.getCode(), GenerateUUID.generateUUID().substring(0, 5));
            } else {
                code = applicationDeployDTO.getInstanceName();

            }
        } else {
            code = applicationInstanceE.getCode();
            //更新实例的时候校验gitops库文件是否存在,处理部署实例时，由于没有创gitops文件导致的部署失败
            checkOptionsHandler.check(devopsEnvironmentE, applicationDeployDTO.getAppInstanceId(), code, C7NHELM_RELEASE);
        }

        //检验gitops库是否存在，校验操作人是否是有gitops库的权限
        UserAttrE userAttrE = userAttrRepository.queryById(TypeUtil.objToLong(GitUserNameUtil.getUserId()));
        gitlabGroupMemberService.checkEnvProject(devopsEnvironmentE, userAttrE);

        //更新时候，如果isNotChange的值为true，则直接向agent发送更新指令，不走gitops,否则走操作gitops库文件逻辑
        if (applicationDeployDTO.getIsNotChange()) {
            applicationInstanceRepository.update(applicationInstanceE);
            applicationInstanceE = applicationInstanceRepository.selectById(applicationDeployDTO.getAppInstanceId());
            devopsEnvCommandE = devopsEnvCommandRepository.query(applicationInstanceE.getCommandId());
            devopsEnvCommandE.setId(null);
            devopsEnvCommandE.setCommandType(CommandType.UPDATE.getType());
            devopsEnvCommandE.setStatus(CommandStatus.OPERATING.getStatus());
            deployService.deploy(applicationE, applicationVersionE, applicationInstanceE.getCode(), devopsEnvironmentE,
                    devopsEnvCommandValueE.getValue(), devopsEnvCommandRepository.create(devopsEnvCommandE).getId());
        } else {
            //存储数据
            if (applicationDeployDTO.getType().equals(CREATE)) {
                applicationInstanceE.setCode(code);
                applicationInstanceE.setId(applicationInstanceRepository.create(applicationInstanceE).getId());
                devopsEnvCommandE.setObjectId(applicationInstanceE.getId());
                devopsEnvCommandE.initDevopsEnvCommandValueE(
                        devopsEnvCommandValueRepository.create(devopsEnvCommandValueE).getId());
                applicationInstanceE.setCommandId(devopsEnvCommandRepository.create(devopsEnvCommandE).getId());
                applicationInstanceRepository.update(applicationInstanceE);
            } else {
                devopsEnvCommandE.setObjectId(applicationInstanceE.getId());
                devopsEnvCommandE.initDevopsEnvCommandValueE(
                        devopsEnvCommandValueRepository.create(devopsEnvCommandValueE).getId());
                applicationInstanceE.setCommandId(devopsEnvCommandRepository.create(devopsEnvCommandE).getId());
                applicationInstanceRepository.update(applicationInstanceE);
            }

            //判断当前容器目录下是否存在环境对应的gitops文件目录，不存在则克隆
            String filePath = envUtil.handDevopsEnvGitRepository(devopsEnvironmentE);

            //在gitops库处理instance文件
            ObjectOperation<C7nHelmRelease> objectOperation = new ObjectOperation<>();
            objectOperation.setType(getC7NHelmRelease(
                    code, applicationVersionE, applicationDeployDTO, applicationE));
            Integer projectId = TypeUtil.objToInteger(devopsEnvironmentE.getGitlabEnvProjectId());
            objectOperation.operationEnvGitlabFile(
                    RELEASE_PREFIX + code,
                    projectId,
                    applicationDeployDTO.getType(),
                    userAttrE.getGitlabUserId(),
                    applicationInstanceE.getId(), C7NHELM_RELEASE, null, devopsEnvironmentE.getId(), filePath);

        }
        return ConvertHelper.convert(applicationInstanceE, ApplicationInstanceDTO.class);
    }

    @Override
    public ApplicationInstanceDTO createOrUpdateByGitOps(ApplicationDeployDTO applicationDeployDTO, Long userId) {
        DevopsEnvironmentE devopsEnvironmentE = devopsEnvironmentRepository
                .queryById(applicationDeployDTO.getEnvironmentId());
        //校验环境是否连接
        envUtil.checkEnvConnection(devopsEnvironmentE.getClusterE().getId(), envListener);

        //校验values
        FileUtil.checkYamlFormat(applicationDeployDTO.getValues());

        //初始化ApplicationInstanceE,DevopsEnvCommandE,DevopsEnvCommandValueE
        ApplicationInstanceE applicationInstanceE = initApplicationInstanceE(applicationDeployDTO);
        DevopsEnvCommandE devopsEnvCommandE = initDevopsEnvCommandE(applicationDeployDTO);
        DevopsEnvCommandValueE devopsEnvCommandValueE = initDevopsEnvCommandValueE(applicationDeployDTO);

        //实例相关对象数据库操作
        if (applicationDeployDTO.getType().equals(CREATE)) {
            applicationInstanceE.setCode(applicationDeployDTO.getInstanceName());
            applicationInstanceE.setId(applicationInstanceRepository.create(applicationInstanceE).getId());
        } else {
            applicationInstanceRepository.update(applicationInstanceE);
        }
        devopsEnvCommandE.setCreatedBy(userId);
        devopsEnvCommandE.setObjectId(applicationInstanceE.getId());
        devopsEnvCommandE.initDevopsEnvCommandValueE(
                devopsEnvCommandValueRepository.create(devopsEnvCommandValueE).getId());
        applicationInstanceE.setCommandId(devopsEnvCommandRepository.create(devopsEnvCommandE).getId());
        applicationInstanceRepository.update(applicationInstanceE);
        return ConvertHelper.convert(applicationInstanceE, ApplicationInstanceDTO.class);
    }

    private ApplicationInstanceE initApplicationInstanceE(ApplicationDeployDTO applicationDeployDTO) {

        ApplicationInstanceE applicationInstanceE = new ApplicationInstanceE();
        applicationInstanceE.initApplicationEById(applicationDeployDTO.getAppId());
        applicationInstanceE.initDevopsEnvironmentEById(applicationDeployDTO.getEnvironmentId());
        applicationInstanceE.setStatus(InstanceStatus.OPERATIING.getStatus());
        if (applicationDeployDTO.getType().equals(UPDATE)) {
            ApplicationInstanceE newApplicationInstanceE = applicationInstanceRepository.selectById(
                    applicationDeployDTO.getAppInstanceId());
            applicationInstanceE.setCode(newApplicationInstanceE.getCode());
            applicationInstanceE.setId(applicationDeployDTO.getAppInstanceId());
        }
        return applicationInstanceE;
    }

    private DevopsEnvCommandE initDevopsEnvCommandE(ApplicationDeployDTO applicationDeployDTO) {
        DevopsEnvCommandE devopsEnvCommandE = new DevopsEnvCommandE();
        switch (applicationDeployDTO.getType()) {
            case CREATE:
                devopsEnvCommandE.setCommandType(CommandType.CREATE.getType());
                break;
            case UPDATE:
                devopsEnvCommandE.setCommandType(CommandType.UPDATE.getType());
                break;
            default:
                devopsEnvCommandE.setCommandType(CommandType.DELETE.getType());
                break;
        }
        devopsEnvCommandE.setObjectVersionId(applicationDeployDTO.getAppVersionId());
        devopsEnvCommandE.setObject(ObjectType.INSTANCE.getType());
        devopsEnvCommandE.setStatus(CommandStatus.OPERATING.getStatus());
        return devopsEnvCommandE;
    }

    private DevopsEnvCommandValueE initDevopsEnvCommandValueE(ApplicationDeployDTO applicationDeployDTO) {
        DevopsEnvCommandValueE devopsEnvCommandValueE = new DevopsEnvCommandValueE();
        devopsEnvCommandValueE.setValue(
                getReplaceResult(applicationVersionRepository.queryValue(applicationDeployDTO.getAppVersionId()),
                        applicationDeployDTO.getValues()).getDeltaYaml().trim());
        return devopsEnvCommandValueE;
    }

    @Override
    public List<AppInstanceCodeDTO> listByOptions(Long projectId, Long appId, Long appVersionId, Long envId) {
        return ConvertHelper.convertList(applicationInstanceRepository
                .listByOptions(projectId, appId, appVersionId, envId), AppInstanceCodeDTO.class);
    }

    @Override
    public List<AppInstanceCodeDTO> listByAppIdAndEnvId(Long projectId, Long appId, Long envId) {
        return ConvertHelper.convertList(applicationInstanceRepository
                .listByAppIdAndEnvId(projectId, appId, envId), AppInstanceCodeDTO.class);
    }

    @Override
    public void instanceStop(Long instanceId) {
        ApplicationInstanceE instanceE = applicationInstanceRepository.selectById(instanceId);
        //校验用户是否有环境的权限
        DevopsEnvironmentE devopsEnvironmentE = checkEnvPermission(instanceE.getDevopsEnvironmentE().getId());
        if (!instanceE.getStatus().equals(InstanceStatus.RUNNING.getStatus())) {
            throw new CommonException("error.instance.notRunning");
        }
        DevopsEnvCommandE devopsEnvCommandE = devopsEnvCommandRepository
                .queryByObject(ObjectType.INSTANCE.getType(), instanceId);
        devopsEnvCommandE.setCommandType(CommandType.STOP.getType());
        devopsEnvCommandE.setStatus(CommandStatus.OPERATING.getStatus());
        devopsEnvCommandE.setId(null);
        devopsEnvCommandE = devopsEnvCommandRepository.create(devopsEnvCommandE);
        String namespace = getNameSpace(instanceE.getDevopsEnvironmentE().getId());
        String releaseName = updateInstanceStatus(instanceId, InstanceStatus.OPERATIING.getStatus());
        Map<String, String> stopMap = new HashMap<>();
        stopMap.put(RELEASE_NAME, releaseName);
        String payload = gson.toJson(stopMap);
        Long envId = instanceE.getDevopsEnvironmentE().getId();
        sentInstance(payload, releaseName, HelmType.HELM_RELEASE_STOP.toValue(),
                namespace, devopsEnvCommandE.getId(), envId, devopsEnvironmentE.getClusterE().getId());
    }

    @Override
    public void instanceStart(Long instanceId) {
        ApplicationInstanceE instanceE = applicationInstanceRepository.selectById(instanceId);
        //校验用户是否有环境的权限
        DevopsEnvironmentE devopsEnvironmentE = checkEnvPermission(instanceE.getDevopsEnvironmentE().getId());
        if (!instanceE.getStatus().equals(InstanceStatus.STOPPED.getStatus())) {
            throw new CommonException("error.instance.notStop");
        }
        DevopsEnvCommandE devopsEnvCommandE = devopsEnvCommandRepository
                .queryByObject(ObjectType.INSTANCE.getType(), instanceId);
        devopsEnvCommandE.setCommandType(CommandType.RESTART.getType());
        devopsEnvCommandE.setStatus(CommandStatus.OPERATING.getStatus());
        devopsEnvCommandE.setId(null);
        devopsEnvCommandE = devopsEnvCommandRepository.create(devopsEnvCommandE);
        String namespace = getNameSpace(instanceE.getDevopsEnvironmentE().getId());
        String releaseName = updateInstanceStatus(instanceId, InstanceStatus.OPERATIING.getStatus());
        Map<String, String> stopMap = new HashMap<>();
        stopMap.put(RELEASE_NAME, releaseName);
        String payload = gson.toJson(stopMap);
        Long envId = instanceE.getDevopsEnvironmentE().getId();
        sentInstance(payload, releaseName, HelmType.HELM_RELEASE_START.toValue(),
                namespace, devopsEnvCommandE.getId(), envId, devopsEnvironmentE.getClusterE().getId());
    }

    @Override
    public void instanceReStart(Long instanceId) {
        ApplicationInstanceE instanceE = applicationInstanceRepository.selectById(instanceId);
        //校验用户是否有环境的权限
        devopsEnvUserPermissionRepository.checkEnvDeployPermission(TypeUtil.objToLong(GitUserNameUtil.getUserId()),
                instanceE.getDevopsEnvironmentE().getId());
        DevopsEnvCommandE devopsEnvCommandE = devopsEnvCommandRepository.query(instanceE.getCommandId());
        ApplicationE applicationE = applicationRepository.query(instanceE.getApplicationE().getId());
        ApplicationVersionE applicationVersionE = applicationVersionRepository
                .query(devopsEnvCommandE.getObjectVersionId());
        DevopsEnvironmentE devopsEnvironmentE = devopsEnvironmentRepository
                .queryById(instanceE.getDevopsEnvironmentE().getId());
        String value = applicationInstanceRepository.queryValueByInstanceId(instanceId);
        instanceE.setStatus(InstanceStatus.OPERATIING.getStatus());
        devopsEnvCommandE.setId(null);
        devopsEnvCommandE.setCommandType(CommandType.UPDATE.getType());
        devopsEnvCommandE.setStatus(CommandStatus.OPERATING.getStatus());
        Long commandId = devopsEnvCommandRepository.create(devopsEnvCommandE).getId();
        instanceE.setCommandId(commandId);
        applicationInstanceRepository.update(instanceE);
        deployService.deploy(applicationE, applicationVersionE, instanceE.getCode(), devopsEnvironmentE, value, commandId);
    }

    @Override
    public void instanceDelete(Long instanceId) {
        ApplicationInstanceE instanceE = applicationInstanceRepository.selectById(instanceId);
        //校验用户是否有环境的权限
        DevopsEnvironmentE devopsEnvironmentE = checkEnvPermission(instanceE.getDevopsEnvironmentE().getId());

        DevopsEnvCommandE devopsEnvCommandE;
        if (instanceE.getCommandId() == null) {
            devopsEnvCommandE = devopsEnvCommandRepository
                    .queryByObject(ObjectType.INSTANCE.getType(), instanceE.getId());
        } else {
            devopsEnvCommandE = devopsEnvCommandRepository
                    .query(instanceE.getCommandId());
        }
        devopsEnvCommandE.setCommandType(CommandType.DELETE.getType());
        devopsEnvCommandE.setStatus(CommandStatus.OPERATING.getStatus());
        devopsEnvCommandE.setId(null);

        //检验gitops库是否存在，校验操作人是否是有gitops库的权限
        UserAttrE userAttrE = userAttrRepository.queryById(TypeUtil.objToLong(GitUserNameUtil.getUserId()));
        gitlabGroupMemberService.checkEnvProject(devopsEnvironmentE, userAttrE);

        //实例相关对象数据库操作
        instanceE.setStatus(InstanceStatus.OPERATIING.getStatus());
        instanceE.setCommandId(devopsEnvCommandRepository.create(devopsEnvCommandE).getId());
        applicationInstanceRepository.update(instanceE);

        //判断当前容器目录下是否存在环境对应的gitops文件目录，不存在则克隆
        String path = envUtil.handDevopsEnvGitRepository(devopsEnvironmentE);

        //如果对象所在文件只有一个对象，则直接删除文件,否则把对象从文件中去掉，更新文件
        DevopsEnvFileResourceE devopsEnvFileResourceE = devopsEnvFileResourceRepository
                .queryByEnvIdAndResource(devopsEnvironmentE.getId(), instanceId, C7NHELM_RELEASE);
        if (devopsEnvFileResourceE == null) {
            applicationInstanceRepository.deleteById(instanceId);
            if (gitlabRepository.getFile(TypeUtil.objToInteger(devopsEnvironmentE.getGitlabEnvProjectId()), "master",
                    RELEASE_PREFIX + instanceE.getCode() + YAML_SUFFIX)) {
                gitlabRepository.deleteFile(
                        TypeUtil.objToInteger(devopsEnvironmentE.getGitlabEnvProjectId()),
                        RELEASE_PREFIX + instanceE.getCode() + YAML_SUFFIX,
                        "DELETE FILE",
                        TypeUtil.objToInteger(userAttrE.getGitlabUserId()));
            }
            return;
        }
        List<DevopsEnvFileResourceE> devopsEnvFileResourceES = devopsEnvFileResourceRepository
                .queryByEnvIdAndPath(devopsEnvironmentE.getId(), devopsEnvFileResourceE.getFilePath());
        if (devopsEnvFileResourceES.size() == 1) {
            if (gitlabRepository.getFile(TypeUtil.objToInteger(devopsEnvironmentE.getGitlabEnvProjectId()), "master",
                    devopsEnvFileResourceE.getFilePath())) {
                gitlabRepository.deleteFile(
                        TypeUtil.objToInteger(devopsEnvironmentE.getGitlabEnvProjectId()),
                        devopsEnvFileResourceE.getFilePath(),
                        "DELETE FILE",
                        TypeUtil.objToInteger(userAttrE.getGitlabUserId()));
            }
        } else {
            ObjectOperation<C7nHelmRelease> objectOperation = new ObjectOperation<>();
            C7nHelmRelease c7nHelmRelease = new C7nHelmRelease();
            Metadata metadata = new Metadata();
            metadata.setName(instanceE.getCode());
            c7nHelmRelease.setMetadata(metadata);
            objectOperation.setType(c7nHelmRelease);
            Integer projectId = TypeUtil.objToInteger(devopsEnvironmentE.getGitlabEnvProjectId());
            objectOperation.operationEnvGitlabFile(
                    RELEASE_PREFIX + instanceE.getCode(),
                    projectId,
                    "delete",
                    userAttrE.getGitlabUserId(),
                    instanceE.getId(), C7NHELM_RELEASE, null, devopsEnvironmentE.getId(), path);
        }
        devopsAutoDeployRepository.deleteInstanceId(instanceId);
    }

    @Override
    public void instanceDeleteByGitOps(Long instanceId) {
        ApplicationInstanceE instanceE = applicationInstanceRepository.selectById(instanceId);

        DevopsEnvironmentE devopsEnvironmentE = devopsEnvironmentRepository
                .queryById(instanceE.getDevopsEnvironmentE().getId());

        //校验环境是否连接
        envUtil.checkEnvConnection(devopsEnvironmentE.getClusterE().getId(), envListener);

        //实例相关对象数据库操作
        DevopsEnvCommandE devopsEnvCommandE;
        if (instanceE.getCommandId() == null) {
            devopsEnvCommandE = devopsEnvCommandRepository
                    .queryByObject(ObjectType.INSTANCE.getType(), instanceE.getId());
        } else {
            devopsEnvCommandE = devopsEnvCommandRepository
                    .query(instanceE.getCommandId());
        }
        devopsEnvCommandE.setStatus(CommandStatus.SUCCESS.getStatus());
        devopsEnvCommandRepository.update(devopsEnvCommandE);
        applicationInstanceRepository.deleteById(instanceId);
    }

    @Override
    public void checkName(String instanceName) {
        AppInstanceValidator.checkName(instanceName);
        ApplicationInstanceE applicationInstanceE = new ApplicationInstanceE();
        applicationInstanceE.setCode(instanceName);
        applicationInstanceRepository.checkName(instanceName);
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

    private void sentInstance(String payload, String name, String type, String namespace, Long commandId, Long envId,
                              Long clusterId) {
        Msg msg = new Msg();
        msg.setKey("cluster:" + clusterId + ".env:" + namespace + ".envId:" + envId + ".release:" + name);
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
        applicationInstanceES.forEach(applicationInstanceE ->
                applicationInstanceE.setConnect(envSessionMap.entrySet().stream()
                        .anyMatch(entry -> {
                            EnvSession envSession = entry.getValue();
                            DevopsEnvironmentE devopsEnvironmentE = devopsEnvironmentRepository
                                    .queryById(applicationInstanceE.getDevopsEnvironmentE().getId());
                            return envSession.getClusterId().equals(devopsEnvironmentE.getClusterE().getId())
                                    && EnvUtil.compareVersion(envSession.getVersion(), agentExpectVersion) != 1;
                        })));
    }

    private C7nHelmRelease getC7NHelmRelease(String code, ApplicationVersionE applicationVersionE,
                                             ApplicationDeployDTO applicationDeployDTO, ApplicationE applicationE) {
        C7nHelmRelease c7nHelmRelease = new C7nHelmRelease();
        c7nHelmRelease.getMetadata().setName(code);
        c7nHelmRelease.getSpec().setRepoUrl(helmUrl + applicationVersionE.getRepository());
        c7nHelmRelease.getSpec().setChartName(applicationE.getCode());
        c7nHelmRelease.getSpec().setChartVersion(applicationVersionE.getVersion());
        c7nHelmRelease.getSpec().setValues(
                getReplaceResult(applicationVersionRepository.queryValue(applicationDeployDTO.getAppVersionId()),
                        applicationDeployDTO.getValues()).getDeltaYaml().trim());
        return c7nHelmRelease;
    }

    @Override
    public ReplaceResult getReplaceResult(String versionValue, String deployValue) {
        if (versionValue.equals(deployValue) || deployValue.equals("")) {
            ReplaceResult replaceResult = new ReplaceResult();
            replaceResult.setDeltaYaml("");
            replaceResult.setYaml(versionValue);
            replaceResult.setHighlightMarkers(new ArrayList<>());
            replaceResult.setNewLines(new ArrayList<>());
            return replaceResult;
        }

        String fileName = GenerateUUID.generateUUID() + YAML_SUFFIX;
        String path = "deployfile";
        FileUtil.saveDataToFile(path, fileName, versionValue + "\n" + "---" + "\n" + deployValue);
        ReplaceResult replaceResult;
        try {
            replaceResult = FileUtil.replaceNew(path + System.getProperty(FILE_SEPARATOR) + fileName);
        } catch (Exception e) {
            throw new CommonException(e.getMessage(), e);
        }
        if (replaceResult.getHighlightMarkers() == null) {
            replaceResult.setHighlightMarkers(new ArrayList<>());
        }
        replaceResult.setTotalLine(FileUtil.getFileTotalLine(replaceResult.getYaml()));
        FileUtil.deleteFile(path + System.getProperty(FILE_SEPARATOR) + fileName);
        return replaceResult;
    }

    private String getDeployTime(Long diff) {
        float num = (float) diff / (60 * 1000);
        DecimalFormat df = new DecimalFormat("0.00");
        return df.format(num);
    }


}
