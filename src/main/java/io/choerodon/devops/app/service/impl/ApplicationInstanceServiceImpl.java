package io.choerodon.devops.app.service.impl;


import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.*;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.pagehelper.PageInfo;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import io.choerodon.asgard.saga.annotation.Saga;
import io.choerodon.asgard.saga.dto.StartInstanceDTO;
import io.choerodon.asgard.saga.feign.SagaClient;
import io.choerodon.base.domain.PageRequest;
import io.choerodon.core.convertor.ConvertHelper;
import io.choerodon.core.convertor.ConvertPageHelper;
import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.validator.AppInstanceValidator;
import io.choerodon.devops.api.vo.*;
import io.choerodon.devops.app.service.ApplicationInstanceService;
import io.choerodon.devops.app.service.DeployService;
import io.choerodon.devops.app.service.DevopsEnvResourceService;
import io.choerodon.devops.app.service.DevopsEnvironmentService;
import io.choerodon.devops.domain.application.repository.*;
import io.choerodon.devops.domain.application.valueobject.*;
import io.choerodon.devops.infra.dataobject.*;
import io.choerodon.devops.infra.enums.*;
import io.choerodon.devops.infra.mapper.ApplicationInstanceMapper;
import io.choerodon.devops.infra.mapper.DevopsEnvApplicationMapper;
import io.choerodon.devops.infra.util.*;
import io.choerodon.websocket.Msg;
import io.choerodon.websocket.helper.CommandSender;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.core.io.InputStreamResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


/**
 * Created by Zenger on 2018/4/12.
 */
@Service
public class ApplicationInstanceServiceImpl implements ApplicationInstanceService {

    public static final String CREATE = "create";
    public static final String UPDATE = "update";
    public static final String CHOERODON = "choerodon-test";
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
    private ApplicationVersionValueRepository versionValueRepository;
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
    private EnvUtil envUtil;
    @Autowired
    private UserAttrRepository userAttrRepository;
    @Autowired
    private ApplicationInstanceMapper applicationInstanceMapper;
    @Autowired
    private ApplicationVersionReadmeRepository versionReadmeRepository;
    @Autowired
    private DevopsEnvPodRepository devopsEnvPodRepository;
    @Autowired
    private DevopsEnvResourceService devopsEnvResourceService;
    @Autowired
    private GitlabRepository gitlabRepository;
    @Autowired
    private DevopsEnvUserPermissionRepository devopsEnvUserPermissionRepository;
    @Autowired
    private ResourceFileCheckHandler resourceFileCheckHandler;
    @Autowired
    private DevopsProjectConfigRepository devopsProjectConfigRepository;
    @Autowired
    private DevopsRegistrySecretRepository devopsRegistrySecretRepository;
    @Autowired
    private PipelineAppDeployRepository appDeployRepository;
    @Autowired
    private DevopsDeployValueRepository devopsDeployValueRepository;
    @Autowired
    private DevopsEnvironmentService devopsEnvironmentService;
    @Autowired
    private SagaClient sagaClient;
    @Autowired
    private DevopsEnvApplicationMapper envApplicationMapper;


    @Override
    public AppInstanceInfoVO queryInfoById(Long instanceId) {
        AppInstanceInfoDTO appInstanceInfoDTO = applicationInstanceMapper.queryInfoById(instanceId);
        if (appInstanceInfoDTO == null) {
            return null;
        }
        AppInstanceInfoVO appInstanceInfoVO = new AppInstanceInfoVO();
        BeanUtils.copyProperties(appInstanceInfoDTO, appInstanceInfoVO);
        return appInstanceInfoVO;
    }

    @Override
    public PageInfo<DevopsEnvPreviewInstanceVO> pageByOptions(Long projectId, PageRequest pageRequest,
                                                              Long envId, Long versionId, Long appId, Long instanceId, String params) {
        List<Long> connectedEnvList = envUtil.getConnectedEnvList();
        List<Long> updatedEnvList = envUtil.getUpdatedEnvList();
        PageInfo<ApplicationInstanceE> applicationInstanceEPage = applicationInstanceRepository.listApplicationInstance(
                projectId, pageRequest, envId, versionId, appId, instanceId, params);

        List<ApplicationInstanceE> applicationInstanceES = applicationInstanceEPage.getList();
        setInstanceConnect(applicationInstanceES, connectedEnvList, updatedEnvList);

        PageInfo<ApplicationInstanceDTO> applicationInstanceDTOS = ConvertPageHelper
                .convertPageInfo(applicationInstanceEPage, ApplicationInstanceDTO.class);
        PageInfo<DevopsEnvPreviewInstanceVO> devopsEnvPreviewInstanceDTOPageInfo = new PageInfo<>();
        BeanUtils.copyProperties(applicationInstanceDTOS, devopsEnvPreviewInstanceDTOPageInfo);
        return devopsEnvPreviewInstanceDTOPageInfo;
    }


    @Override
    public List<ApplicationInstancesDTO> listApplicationInstances(Long projectId, Long appId) {

        List<Long> permissionEnvIds = devopsEnvUserPermissionRepository
                .listByUserId(TypeUtil.objToLong(GitUserNameUtil.getUserId())).stream()
                .filter(DevopsEnvUserPermissionE::getPermitted).map(DevopsEnvUserPermissionE::getEnvId)
                .collect(Collectors.toList());

        ProjectVO projectE = iamRepository.queryIamProject(projectId);
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
    public ReplaceResult queryValues(String type, Long instanceId, Long versionId) {
        ReplaceResult replaceResult = new ReplaceResult();
        String versionValue = FileUtil.checkValueFormat(applicationVersionRepository.queryValue(versionId));

        if (type.equals(UPDATE)) {
            ApplicationInstanceE applicationInstanceE = applicationInstanceRepository.selectById(instanceId);
            if (applicationInstanceE.getValueId() == null) {
                replaceResult.setYaml(getReplaceResult(versionValue, applicationInstanceRepository.queryValueByInstanceId(instanceId)).getYaml());
            } else {
                DevopsDeployValueE devopsDeployValueE = devopsDeployValueRepository.queryById(applicationInstanceE.getValueId());
                replaceResult.setYaml(getReplaceResult(versionValue, devopsDeployValueE.getValue()).getYaml());
                replaceResult.setName(devopsDeployValueE.getName());
                replaceResult.setId(devopsDeployValueE.getId());
                replaceResult.setObjectVersionNumber(devopsDeployValueE.getObjectVersionNumber());
            }
        } else {
            try {
                FileUtil.checkYamlFormat(versionValue);
            } catch (Exception e) {
                replaceResult.setYaml(versionValue);
                return replaceResult;
            }
            replaceResult.setYaml(versionValue);
        }
        return replaceResult;
    }

    @Override
    public ReplaceResult queryUpgradeValue(Long instanceId, Long versionId) {
        ApplicationInstanceE applicationInstanceE = applicationInstanceRepository.selectById(instanceId);
        String yaml = FileUtil.checkValueFormat(applicationInstanceRepository.queryValueByInstanceId(instanceId));
        String versionValue = applicationVersionRepository.queryValue(versionId);
        ReplaceResult replaceResult = new ReplaceResult();
        if (applicationInstanceE.getValueId() != null) {
            DevopsDeployValueE devopsDeployValueE = devopsDeployValueRepository.queryById(applicationInstanceE.getValueId());
            replaceResult.setName(devopsDeployValueE.getName());
            replaceResult.setId(devopsDeployValueE.getId());
            replaceResult.setObjectVersionNumber(devopsDeployValueE.getObjectVersionNumber());
        }
        replaceResult.setYaml(getReplaceResult(versionValue, yaml).getYaml());

        return replaceResult;
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
    public PageInfo<DeployDetailDTO> pageDeployFrequencyDetail(Long projectId, PageRequest pageRequest, Long[] envIds,
                                                               Long appId, Date startTime, Date endTime) {
        if (envIds.length == 0) {
            return new PageInfo<>();
        }
        PageInfo<DeployDO> deployDOS = applicationInstanceRepository.pageDeployFrequencyDetail(projectId, pageRequest,
                envIds, appId, startTime, endTime);
        return getDeployDetailDTOS(deployDOS);
    }

    @Override
    public PageInfo<DeployDetailDTO> pageDeployTimeDetail(Long projectId, PageRequest pageRequest, Long[] appIds,
                                                          Long envId,
                                                          Date startTime, Date endTime) {
        if (appIds.length == 0) {
            return new PageInfo<>();
        }
        PageInfo<DeployDO> deployDOS = applicationInstanceRepository.pageDeployTimeDetail(projectId, pageRequest, envId,
                appIds, startTime, endTime);
        return getDeployDetailDTOS(deployDOS);
    }

    @Override
    public void deployTestApp(ApplicationDeployDTO applicationDeployDTO) {
        String versionValue = applicationVersionRepository.queryValue(applicationDeployDTO.getAppVersionId());
        ApplicationE applicationE = applicationRepository.query(applicationDeployDTO.getAppId());

        String secretCode = null;
        secretCode = getSecret(applicationE, secretCode, CHOERODON, null, applicationDeployDTO.getEnvironmentId());

        ApplicationVersionE applicationVersionE = applicationVersionRepository.query(applicationDeployDTO.getAppVersionId());
        FileUtil.checkYamlFormat(applicationDeployDTO.getValues());
        String deployValue = getReplaceResult(versionValue,
                applicationDeployDTO.getValues()).getDeltaYaml().trim();
        deployService.deployTestApp(applicationE, applicationVersionE, applicationDeployDTO.getInstanceName(), secretCode, applicationDeployDTO.getEnvironmentId(), deployValue);
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

        DevopsEnvironmentE devopsEnvironmentE = devopsEnvironmentRepository.queryById(envId);

        UserAttrE userAttrE = userAttrRepository.queryById(TypeUtil.objToLong(GitUserNameUtil.getUserId()));

        //校验环境相关信息
        devopsEnvironmentService.checkEnv(devopsEnvironmentE, userAttrE);

        //不能减少到0
        if (count == 0) {
            return;
        }
        deployService.operatePodCount(deploymentName, devopsEnvironmentE.getCode(), devopsEnvironmentE.getClusterE().getId(), count);
    }


    private PageInfo<DeployDetailDTO> getDeployDetailDTOS(PageInfo<DeployDO> deployDOS) {
        PageInfo<DeployDetailDTO> pageDeployDetailDTOS = new PageInfo<>();
        List<DeployDetailDTO> deployDetailDTOS = new ArrayList<>();
        BeanUtils.copyProperties(deployDOS, pageDeployDetailDTOS);
        deployDOS.getList().forEach(deployDO -> {
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
        pageDeployDetailDTOS.setList(deployDetailDTOS);
        return pageDeployDetailDTOS;
    }

    @Override
    public ReplaceResult queryValue(Long instanceId) {
        ReplaceResult replaceResult = new ReplaceResult();
        String yaml = FileUtil.checkValueFormat(applicationInstanceRepository.queryValueByInstanceId(
                instanceId));
        replaceResult.setYaml(yaml);
        return replaceResult;
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
                yamlPropertySourceLoader.load("test", inputStreamResource);
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
    public DevopsEnvPreviewDTO listByEnv(Long projectId, Long envId, String params) {

        Map<String, Object> maps = gson.fromJson(params, new TypeToken<Map<String, Object>>() {
        }.getType());
        List<Long> connectedEnvList = envUtil.getConnectedEnvList();
        List<Long> updatedEnvList = envUtil.getUpdatedEnvList();

        Map<String, Object> searchParamMap = TypeUtil.cast(maps.get(TypeUtil.SEARCH_PARAM));
        String paramMap = TypeUtil.cast(maps.get(TypeUtil.PARAM));

        List<ApplicationInstanceDO> applicationInstancesDOS = applicationInstanceMapper
                .listApplicationInstance(projectId, envId, null, null, null, searchParamMap, paramMap);
        List<ApplicationInstanceE> applicationInstanceES = ConvertHelper
                .convertList(applicationInstancesDOS, ApplicationInstanceE.class);

        setInstanceConnect(applicationInstanceES, connectedEnvList, updatedEnvList);
        Map<Long, List<ApplicationInstanceE>> resultMaps = applicationInstanceES.stream()
                .collect(Collectors.groupingBy(t -> t.getApplicationE().getId()));
        DevopsEnvPreviewDTO devopsEnvPreviewDTO = new DevopsEnvPreviewDTO();
        List<DevopsEnvPreviewAppDTO> devopsEnvPreviewAppDTOS = new ArrayList<>();
        resultMaps.forEach((key, value) -> {
            DevopsEnvPreviewAppDTO devopsEnvPreviewAppDTO = new DevopsEnvPreviewAppDTO();
            devopsEnvPreviewAppDTO.setAppName(value.get(0).getApplicationE().getName());
            devopsEnvPreviewAppDTO.setAppCode(value.get(0).getAppCode());
            devopsEnvPreviewAppDTO.setProjectId(value.get(0).getProjectId());
            List<ApplicationInstanceDTO> applicationInstanceDTOS = ConvertHelper
                    .convertList(value, ApplicationInstanceDTO.class);

            // set instances
            devopsEnvPreviewAppDTO.setApplicationInstanceDTOS(applicationInstanceDTOS);

            devopsEnvPreviewAppDTOS.add(devopsEnvPreviewAppDTO);
        });
        devopsEnvPreviewDTO.setDevopsEnvPreviewAppDTOS(devopsEnvPreviewAppDTOS);
        return devopsEnvPreviewDTO;
    }


    @Override
    public DevopsEnvResourceDTO listResourcesInHelmRelease(Long instanceId) {

        // 获取相关的pod
        List<DevopsEnvPodDTO> devopsEnvPodDTOS = ConvertHelper
                .convertList(devopsEnvPodRepository.selectByInstanceId(instanceId),
                        DevopsEnvPodDTO.class);

        DevopsEnvResourceDTO devopsEnvResourceDTO = devopsEnvResourceService
                .listResourcesInHelmRelease(instanceId);

        // 关联其pod并设置deployment
        devopsEnvResourceDTO.setDeploymentDTOS(devopsEnvResourceDTO.getDeploymentDTOS()
                .stream()
                .peek(deploymentDTO -> deploymentDTO.setDevopsEnvPodDTOS(filterPodsAssociated(devopsEnvPodDTOS, deploymentDTO.getName())))
                .collect(Collectors.toList())
        );

        // 关联其pod并设置daemonSet
        devopsEnvResourceDTO.setDaemonSetDTOS(
                devopsEnvResourceDTO.getDaemonSetDTOS()
                        .stream()
                        .peek(daemonSetDTO -> daemonSetDTO.setDevopsEnvPodDTOS(
                                filterPodsAssociatedWithDaemonSet(devopsEnvPodDTOS, daemonSetDTO.getName())
                        ))
                        .collect(Collectors.toList())
        );

        // 关联其pod并设置statefulSet
        devopsEnvResourceDTO.setStatefulSetDTOS(
                devopsEnvResourceDTO.getStatefulSetDTOS()
                        .stream()
                        .peek(statefulSetDTO -> statefulSetDTO.setDevopsEnvPodDTOS(
                                filterPodsAssociatedWithStatefulSet(devopsEnvPodDTOS, statefulSetDTO.getName()))
                        )
                        .collect(Collectors.toList())
        );


        return devopsEnvResourceDTO;
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

    @Override
    @Saga(code = "devops-create-instance",
            description = "Devops创建实例", inputSchema = "{}")
    @Transactional(rollbackFor = Exception.class)
    public ApplicationInstanceDTO createOrUpdate(ApplicationDeployDTO applicationDeployDTO) {

        DevopsEnvironmentE devopsEnvironmentE = devopsEnvironmentRepository.queryById(applicationDeployDTO.getEnvironmentId());

        UserAttrE userAttrE = userAttrRepository.queryById(TypeUtil.objToLong(GitUserNameUtil.getUserId()));

        //校验环境相关信息
        devopsEnvironmentService.checkEnv(devopsEnvironmentE, userAttrE);

        //校验values
        FileUtil.checkYamlFormat(applicationDeployDTO.getValues());

        ApplicationE applicationE = applicationRepository.query(applicationDeployDTO.getAppId());
        ApplicationVersionE applicationVersionE =
                applicationVersionRepository.query(applicationDeployDTO.getAppVersionId());

        //初始化ApplicationInstanceE,DevopsEnvCommandE,DevopsEnvCommandValueE
        ApplicationInstanceE applicationInstanceE = initApplicationInstanceE(applicationDeployDTO);
        DevopsEnvCommandE devopsEnvCommandE = initDevopsEnvCommandE(applicationDeployDTO);
        DevopsEnvCommandValueE devopsEnvCommandValueE = initDevopsEnvCommandValueE(applicationDeployDTO);

        String secretCode = null;
        secretCode = getSecret(applicationE, secretCode, devopsEnvironmentE.getCode(), devopsEnvironmentE.getId(), devopsEnvironmentE.getClusterE().getId());


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
            resourceFileCheckHandler.check(devopsEnvironmentE, applicationDeployDTO.getAppInstanceId(), code, C7NHELM_RELEASE);

            //从未关联部署配置到关联部署配置，或者从一个部署配置关联另外一个部署配置，如果values是一样的，虽然getIsNotChange为false,但是此时也应该直接设置为isNotChange为true
            DevopsEnvCommandE oldDevopsEnvCommandE = devopsEnvCommandRepository.query(applicationInstanceRepository.selectById(applicationInstanceE.getId()).getCommandId());
            String deployValue = applicationInstanceRepository.queryValueByInstanceId(applicationInstanceE.getId());
            if (applicationDeployDTO.getAppVersionId().equals(oldDevopsEnvCommandE.getObjectVersionId()) && deployValue.equals(applicationDeployDTO.getValues())) {
                applicationDeployDTO.setIsNotChange(true);
            }
        }

        //更新时候，如果isNotChange的值为true，则直接向agent发送更新指令，不走gitops,否则走操作gitops库文件逻辑

        if (applicationDeployDTO.getIsNotChange()) {
            applicationInstanceE = restartDeploy(applicationDeployDTO, devopsEnvironmentE, applicationE, applicationVersionE, applicationInstanceE, devopsEnvCommandValueE, secretCode);
        } else {
            //存储数据
            if (applicationDeployDTO.getType().equals(CREATE)) {
                // 默认为部署应用的环境和应用之间创建关联关系，如果不存在
                createEnvAppRelationShipIfNon(applicationDeployDTO.getAppId(), applicationDeployDTO.getEnvironmentId());

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
        }
        applicationDeployDTO.setAppInstanceId(applicationInstanceE.getId());
        applicationDeployDTO.setInstanceName(code);
        InstanceSagaDTO instanceSagaDTO = new InstanceSagaDTO(applicationE.getProjectE().getId(), userAttrE.getGitlabUserId(), secretCode);
        instanceSagaDTO.setApplicationE(applicationE);
        instanceSagaDTO.setApplicationVersionE(applicationVersionE);
        instanceSagaDTO.setApplicationDeployDTO(applicationDeployDTO);
        instanceSagaDTO.setDevopsEnvironmentE(devopsEnvironmentE);

        String input = gson.toJson(instanceSagaDTO);

        sagaClient.startSaga("devops-create-instance", new StartInstanceDTO(input, "env", devopsEnvironmentE.getId().toString(), ResourceLevel.PROJECT.value(), devopsEnvironmentE.getProjectE().getId()));

        return ConvertHelper.convert(applicationInstanceE, ApplicationInstanceDTO.class);
    }

    /**
     * 为环境和应用创建关联关系如果不存在
     *
     * @param appId 应用id
     * @param envId 环境id
     */
    private void createEnvAppRelationShipIfNon(Long appId, Long envId) {
        DevopsEnvApplicationDO devopsEnvApplicationDO = new DevopsEnvApplicationDO();
        devopsEnvApplicationDO.setAppId(appId);
        devopsEnvApplicationDO.setEnvId(envId);
        envApplicationMapper.insertIgnore(devopsEnvApplicationDO);
    }

    @Override
    public void createInstanceBySaga(InstanceSagaDTO instanceSagaDTO) {

        try {
            //判断当前容器目录下是否存在环境对应的gitops文件目录，不存在则克隆
            String filePath = envUtil.handDevopsEnvGitRepository(instanceSagaDTO.getProjectId(), instanceSagaDTO.getDevopsEnvironmentE().getCode(), instanceSagaDTO.getDevopsEnvironmentE().getEnvIdRsa());

            //在gitops库处理instance文件
            ResourceConvertToYamlHandler<C7nHelmRelease> resourceConvertToYamlHandler = new ResourceConvertToYamlHandler<>();
            resourceConvertToYamlHandler.setType(getC7NHelmRelease(
                    instanceSagaDTO.getApplicationDeployDTO().getInstanceName(), instanceSagaDTO.getApplicationVersionE().getRepository(), instanceSagaDTO.getApplicationE().getCode(), instanceSagaDTO.getApplicationVersionE().getVersion(), instanceSagaDTO.getApplicationDeployDTO().getValues(), instanceSagaDTO.getApplicationDeployDTO().getAppVersionId(), instanceSagaDTO.getSecretCode()));

            resourceConvertToYamlHandler.operationEnvGitlabFile(
                    RELEASE_PREFIX + instanceSagaDTO.getApplicationDeployDTO().getInstanceName(),
                    instanceSagaDTO.getDevopsEnvironmentE().getGitlabEnvProjectId().intValue(),
                    instanceSagaDTO.getApplicationDeployDTO().getType(),
                    instanceSagaDTO.getGitlabUserId(),
                    instanceSagaDTO.getApplicationDeployDTO().getAppInstanceId(), C7NHELM_RELEASE, null, false, instanceSagaDTO.getDevopsEnvironmentE().getId(), filePath);
        } catch (Exception e) {
            //有异常更新实例以及command的状态
            ApplicationInstanceE applicationInstanceE = applicationInstanceRepository.selectById(instanceSagaDTO.getApplicationDeployDTO().getAppInstanceId());
            applicationInstanceE.setStatus(CommandStatus.FAILED.getStatus());
            applicationInstanceRepository.update(applicationInstanceE);
            DevopsEnvCommandE devopsEnvCommandE = devopsEnvCommandRepository.query(applicationInstanceE.getCommandId());
            devopsEnvCommandE.setStatus(CommandStatus.FAILED.getStatus());
            devopsEnvCommandE.setError("create or update gitOps file failed!");
            devopsEnvCommandRepository.update(devopsEnvCommandE);
        }
    }


    private ApplicationInstanceE restartDeploy(ApplicationDeployDTO applicationDeployDTO, DevopsEnvironmentE devopsEnvironmentE, ApplicationE applicationE, ApplicationVersionE applicationVersionE, ApplicationInstanceE applicationInstanceE, DevopsEnvCommandValueE devopsEnvCommandValueE, String secretCode) {
        DevopsEnvCommandE devopsEnvCommandE;
        applicationInstanceRepository.update(applicationInstanceE);
        applicationInstanceE = applicationInstanceRepository.selectById(applicationDeployDTO.getAppInstanceId());
        devopsEnvCommandE = devopsEnvCommandRepository.query(applicationInstanceE.getCommandId());
        devopsEnvCommandE.setId(null);
        devopsEnvCommandE.setCommandType(CommandType.UPDATE.getType());
        devopsEnvCommandE.setStatus(CommandStatus.OPERATING.getStatus());
        deployService.deploy(applicationE, applicationVersionE, applicationInstanceE.getCode(), devopsEnvironmentE,
                devopsEnvCommandValueE.getValue(), devopsEnvCommandRepository.create(devopsEnvCommandE).getId(), secretCode);
        return applicationInstanceE;
    }

    private String getSecret(ApplicationE applicationE, String secretCode, String namespace, Long envId, Long clusterId) {
        //如果应用绑定了私有镜像库,则处理secret
        if (applicationE.getHarborConfigE() != null) {
            DevopsProjectConfigE devopsProjectConfigE = devopsProjectConfigRepository.queryByPrimaryKey(applicationE.getHarborConfigE().getId());
            if (devopsProjectConfigE.getConfig().getPrivate() != null) {
                DevopsRegistrySecretE devopsRegistrySecretE = devopsRegistrySecretRepository.queryByEnv(namespace, devopsProjectConfigE.getId());
                if (devopsRegistrySecretE == null) {
                    //当配置在当前环境下没有创建过secret.则新增secret信息，并通知k8s创建secret
                    List<DevopsRegistrySecretE> devopsRegistrySecretES = devopsRegistrySecretRepository.listByConfig(devopsProjectConfigE.getId());
                    if (devopsRegistrySecretES.isEmpty()) {
                        secretCode = String.format("%s%s%s%s", "registry-secret-", devopsProjectConfigE.getId(), "-", GenerateUUID.generateUUID().substring(0, 5));
                    } else {
                        secretCode = devopsRegistrySecretES.get(0).getSecretCode();
                    }
                    devopsRegistrySecretE = new DevopsRegistrySecretE(envId, devopsProjectConfigE.getId(), namespace, secretCode, gson.toJson(devopsProjectConfigE.getConfig()));
                    devopsRegistrySecretRepository.create(devopsRegistrySecretE);
                    deployService.operateSecret(clusterId, namespace, secretCode, devopsProjectConfigE.getConfig(), CREATE);
                } else {
                    //判断如果某个配置有发生过修改，则需要修改secret信息，并通知k8s更新secret
                    if (!devopsRegistrySecretE.getSecretDetail().equals(gson.toJson(devopsProjectConfigE.getConfig()))) {
                        devopsRegistrySecretE.setSecretDetail(gson.toJson(devopsProjectConfigE.getConfig()));
                        devopsRegistrySecretRepository.update(devopsRegistrySecretE);
                        deployService.operateSecret(clusterId, namespace, devopsRegistrySecretE.getSecretCode(), devopsProjectConfigE.getConfig(), UPDATE);
                    } else {
                        if (!devopsRegistrySecretE.getStatus()) {
                            deployService.operateSecret(clusterId, namespace, devopsRegistrySecretE.getSecretCode(), devopsProjectConfigE.getConfig(), UPDATE);
                        }
                    }
                    secretCode = devopsRegistrySecretE.getSecretCode();
                }
            }
        }
        return secretCode;
    }

    @Override
    public ApplicationInstanceDTO createOrUpdateByGitOps(ApplicationDeployDTO applicationDeployDTO, Long userId) {
        DevopsEnvironmentE devopsEnvironmentE = devopsEnvironmentRepository
                .queryById(applicationDeployDTO.getEnvironmentId());
        //校验环境是否连接
        envUtil.checkEnvConnection(devopsEnvironmentE.getClusterE().getId());

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
        applicationInstanceE.setValueId(applicationDeployDTO.getValueId());
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
        devopsEnvCommandValueE.setValue(applicationDeployDTO.getValues());
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
    public List<AppInstanceCodeDTO> getByAppIdAndEnvId(Long projectId, Long appId, Long envId) {
        return ConvertHelper.convertList(applicationInstanceRepository
                .getByAppIdAndEnvId(projectId, appId, envId), AppInstanceCodeDTO.class);
    }

    @Override
    public void instanceStop(Long instanceId) {
        ApplicationInstanceE instanceE = applicationInstanceRepository.selectById(instanceId);

        DevopsEnvironmentE devopsEnvironmentE = devopsEnvironmentRepository.queryById(instanceE.getDevopsEnvironmentE().getId());

        UserAttrE userAttrE = userAttrRepository.queryById(TypeUtil.objToLong(GitUserNameUtil.getUserId()));

        //校验环境相关信息
        devopsEnvironmentService.checkEnv(devopsEnvironmentE, userAttrE);
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

        DevopsEnvironmentE devopsEnvironmentE = devopsEnvironmentRepository.queryById(instanceE.getDevopsEnvironmentE().getId());

        UserAttrE userAttrE = userAttrRepository.queryById(TypeUtil.objToLong(GitUserNameUtil.getUserId()));

        //校验环境相关信息
        devopsEnvironmentService.checkEnv(devopsEnvironmentE, userAttrE);

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

        DevopsEnvironmentE devopsEnvironmentE = devopsEnvironmentRepository.queryById(instanceE.getDevopsEnvironmentE().getId());

        UserAttrE userAttrE = userAttrRepository.queryById(TypeUtil.objToLong(GitUserNameUtil.getUserId()));

        //校验环境相关信息
        devopsEnvironmentService.checkEnv(devopsEnvironmentE, userAttrE);

        DevopsEnvCommandE devopsEnvCommandE = devopsEnvCommandRepository.query(instanceE.getCommandId());
        ApplicationE applicationE = applicationRepository.query(instanceE.getApplicationE().getId());
        ApplicationVersionE applicationVersionE = applicationVersionRepository
                .query(devopsEnvCommandE.getObjectVersionId());

        String value = applicationInstanceRepository.queryValueByInstanceId(instanceId);
        instanceE.setStatus(InstanceStatus.OPERATIING.getStatus());
        devopsEnvCommandE.setId(null);
        devopsEnvCommandE.setCommandType(CommandType.UPDATE.getType());
        devopsEnvCommandE.setStatus(CommandStatus.OPERATING.getStatus());
        Long commandId = devopsEnvCommandRepository.create(devopsEnvCommandE).getId();
        instanceE.setCommandId(commandId);
        applicationInstanceRepository.update(instanceE);
        String secretCode = null;
        secretCode = getSecret(applicationE, secretCode, devopsEnvironmentE.getCode(), devopsEnvironmentE.getId(), devopsEnvironmentE.getClusterE().getId());
        deployService.deploy(applicationE, applicationVersionE, instanceE.getCode(), devopsEnvironmentE, value, commandId, secretCode);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void instanceDelete(Long instanceId) {
        ApplicationInstanceE instanceE = applicationInstanceRepository.selectById(instanceId);

        DevopsEnvironmentE devopsEnvironmentE = devopsEnvironmentRepository.queryById(instanceE.getDevopsEnvironmentE().getId());

        UserAttrE userAttrE = userAttrRepository.queryById(TypeUtil.objToLong(GitUserNameUtil.getUserId()));

        //校验环境相关信息
        devopsEnvironmentService.checkEnv(devopsEnvironmentE, userAttrE);

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

        //实例相关对象数据库操作
        instanceE.setStatus(InstanceStatus.OPERATIING.getStatus());
        instanceE.setCommandId(devopsEnvCommandRepository.create(devopsEnvCommandE).getId());
        applicationInstanceRepository.update(instanceE);

        //判断当前容器目录下是否存在环境对应的gitops文件目录，不存在则克隆
        String path = envUtil.handDevopsEnvGitRepository(devopsEnvironmentE.getProjectE().getId(), devopsEnvironmentE.getCode(), devopsEnvironmentE.getEnvIdRsa());

        //如果对象所在文件只有一个对象，则直接删除文件,否则把对象从文件中去掉，更新文件
        DevopsEnvFileResourceE devopsEnvFileResourceE = devopsEnvFileResourceRepository
                .queryByEnvIdAndResource(devopsEnvironmentE.getId(), instanceId, C7NHELM_RELEASE);
        if (devopsEnvFileResourceE == null) {
            applicationInstanceRepository.deleteInstanceRelInfo(instanceId);
            if (gitlabRepository.getFile(TypeUtil.objToInteger(devopsEnvironmentE.getGitlabEnvProjectId()), "master",
                    RELEASE_PREFIX + instanceE.getCode() + YAML_SUFFIX)) {
                gitlabRepository.deleteFile(
                        TypeUtil.objToInteger(devopsEnvironmentE.getGitlabEnvProjectId()),
                        RELEASE_PREFIX + instanceE.getCode() + YAML_SUFFIX,
                        "DELETE FILE",
                        TypeUtil.objToInteger(userAttrE.getGitlabUserId()));
            }
            return;
        } else {
            if (!gitlabRepository.getFile(TypeUtil.objToInteger(devopsEnvironmentE.getGitlabEnvProjectId()), "master",
                    devopsEnvFileResourceE.getFilePath())) {
                applicationInstanceRepository.deleteInstanceRelInfo(instanceId);
                devopsEnvFileResourceRepository.deleteFileResource(devopsEnvFileResourceE.getId());
                return;
            }
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
            ResourceConvertToYamlHandler<C7nHelmRelease> resourceConvertToYamlHandler = new ResourceConvertToYamlHandler<>();
            C7nHelmRelease c7nHelmRelease = new C7nHelmRelease();
            Metadata metadata = new Metadata();
            metadata.setName(instanceE.getCode());
            c7nHelmRelease.setMetadata(metadata);
            resourceConvertToYamlHandler.setType(c7nHelmRelease);
            Integer projectId = TypeUtil.objToInteger(devopsEnvironmentE.getGitlabEnvProjectId());
            resourceConvertToYamlHandler.operationEnvGitlabFile(
                    RELEASE_PREFIX + instanceE.getCode(),
                    projectId,
                    "delete",
                    userAttrE.getGitlabUserId(),
                    instanceE.getId(), C7NHELM_RELEASE, null, false, devopsEnvironmentE.getId(), path);
        }
        appDeployRepository.updateInstanceId(instanceId);
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
    public void instanceDeleteByGitOps(Long instanceId) {
        ApplicationInstanceE instanceE = applicationInstanceRepository.selectById(instanceId);

        DevopsEnvironmentE devopsEnvironmentE = devopsEnvironmentRepository
                .queryById(instanceE.getDevopsEnvironmentE().getId());

        //校验环境是否连接
        envUtil.checkEnvConnection(devopsEnvironmentE.getClusterE().getId());

        applicationInstanceRepository.deleteInstanceRelInfo(instanceId);
        applicationInstanceRepository.deleteById(instanceId);
    }


    @Override
    public void checkName(String instanceName, Long envId) {
        AppInstanceValidator.checkName(instanceName);
        ApplicationInstanceE applicationInstanceE = new ApplicationInstanceE();
        applicationInstanceE.setCode(instanceName);
        applicationInstanceRepository.checkName(instanceName, envId);
        appDeployRepository.checkName(instanceName, envId);
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
                                    List<Long> connectedEnvList, List<Long> updatedEnvList) {
        applicationInstanceES.forEach(applicationInstanceE ->
                {
                    DevopsEnvironmentE devopsEnvironmentE = devopsEnvironmentRepository
                            .queryById(applicationInstanceE.getDevopsEnvironmentE().getId());
                    if (connectedEnvList.contains(devopsEnvironmentE.getClusterE().getId())
                            && updatedEnvList.contains(devopsEnvironmentE.getClusterE().getId())) {
                        applicationInstanceE.setConnect(true);
                    }
                }
        );
    }

    private C7nHelmRelease getC7NHelmRelease(String code, String repository, String appCode, String version, String deployValue, Long deployVersionId,
                                             String secretName) {
        C7nHelmRelease c7nHelmRelease = new C7nHelmRelease();
        c7nHelmRelease.getMetadata().setName(code);
        c7nHelmRelease.getSpec().setRepoUrl(repository);
        c7nHelmRelease.getSpec().setChartName(appCode);
        c7nHelmRelease.getSpec().setChartVersion(version);
        if (secretName != null) {
            c7nHelmRelease.getSpec().setImagePullSecrets(Arrays.asList(new ImagePullSecret(secretName)));
        }
        c7nHelmRelease.getSpec().setValues(
                getReplaceResult(applicationVersionRepository.queryValue(deployVersionId),
                        deployValue).getDeltaYaml().trim());
        return c7nHelmRelease;
    }


    private String getDeployTime(Long diff) {
        float num = (float) diff / (60 * 1000);
        DecimalFormat df = new DecimalFormat("0.00");
        return df.format(num);
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

    @Override
    public ApplicationInstanceDTO deployRemote(ApplicationRemoteDeployDTO appRemoteDeployDTO) {
        ApplicationE applicationE = createApplication(appRemoteDeployDTO);
        ApplicationVersionE versionE = createVersion(applicationE, appRemoteDeployDTO.getVersionRemoteDTO());
        ApplicationDeployDTO applicationDeployDTO = new ApplicationDeployDTO();
        BeanUtils.copyProperties(appRemoteDeployDTO, applicationDeployDTO);
        applicationDeployDTO.setAppId(applicationE.getId());
        applicationDeployDTO.setAppVersionId(versionE.getId());
        applicationDeployDTO.setValues(appRemoteDeployDTO.getVersionRemoteDTO().getValues());
        return createOrUpdate(applicationDeployDTO);
    }


    private ApplicationE createApplication(ApplicationRemoteDeployDTO appRemoteDeployDTO) {
        String code = appRemoteDeployDTO.getAppRemoteDTO().getCode();
        String name = appRemoteDeployDTO.getAppRemoteDTO().getName();
        ApplicationE applicationE = applicationRepository.queryByCodeWithNullProject(code);
        if (applicationE == null) {
            applicationE = new ApplicationE();
            DevopsProjectConfigE harborConfigE = createConfig("harbor", appRemoteDeployDTO.getAppRemoteDTO().getCode(), appRemoteDeployDTO.getHarbor());
            DevopsProjectConfigE chartConfigE = createConfig("chart", appRemoteDeployDTO.getAppRemoteDTO().getCode(), appRemoteDeployDTO.getChart());
            applicationE.setType(appRemoteDeployDTO.getAppRemoteDTO().getType());
            applicationE.setCode(code);
            applicationE.setName(name);
            applicationE.setActive(true);
            applicationE.setSynchro(true);
            applicationE.setIsSkipCheckPermission(true);
            applicationE.initHarborConfig(harborConfigE.getId());
            applicationE.initChartConfig(chartConfigE.getId());
            return applicationRepository.create(applicationE);
        }
        return applicationE;
    }

    private ApplicationVersionE createVersion(ApplicationE applicationE, ApplicationVersionRemoteDTO versionRemoteDTO) {
        ApplicationVersionE versionE = applicationVersionRepository.queryByAppAndVersion(applicationE.getId(), versionRemoteDTO.getVersion());
        if (versionE == null) {
            ApplicationVersionValueE versionValueE = new ApplicationVersionValueE();
            versionValueE.setValue(versionRemoteDTO.getValues());
            versionValueE = versionValueRepository.create(versionValueE);
            ApplicationVersionReadmeV versionReadmeV = new ApplicationVersionReadmeV();
            versionReadmeV.setReadme(versionRemoteDTO.getReadMeValue());
            versionReadmeV = versionReadmeRepository.create(versionReadmeV);
            versionE = new ApplicationVersionE();
            BeanUtils.copyProperties(versionRemoteDTO, versionE);
            versionE.setApplicationE(applicationE);
            versionE.setApplicationVersionValueE(versionValueE);
            versionE.setApplicationVersionReadmeV(versionReadmeV);
            return applicationVersionRepository.create(versionE);
        }
        return versionE;
    }

    /**
     * 创建远程配置
     *
     * @param type
     * @param code
     * @param projectConfigDTO
     * @return
     */
    private DevopsProjectConfigE createConfig(String type, String code, ProjectConfigDTO projectConfigDTO) {
        String name = code + "-" + type;
        DevopsProjectConfigE devopsPrpjectConfigE = devopsProjectConfigRepository.queryByNameWithNullProject(name);
        if (devopsPrpjectConfigE == null) {
            DevopsProjectConfigDTO devopsProjectConfigDTO = new DevopsProjectConfigDTO();
            devopsProjectConfigDTO.setConfig(projectConfigDTO);
            devopsPrpjectConfigE = ConvertHelper.convert(devopsProjectConfigDTO, DevopsProjectConfigE.class);
            devopsPrpjectConfigE.setName(name);
            devopsPrpjectConfigE.setType(type);
            return devopsProjectConfigRepository.create(devopsPrpjectConfigE);
        }
        return devopsPrpjectConfigE;
    }
}
