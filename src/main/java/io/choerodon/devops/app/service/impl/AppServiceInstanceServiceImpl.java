package io.choerodon.devops.app.service.impl;


import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.*;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import io.choerodon.asgard.saga.annotation.Saga;
import io.choerodon.asgard.saga.producer.StartSagaBuilder;
import io.choerodon.asgard.saga.producer.TransactionalProducer;
import io.choerodon.base.domain.PageRequest;
import io.choerodon.core.exception.CommonException;
import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.devops.api.validator.AppInstanceValidator;
import io.choerodon.devops.api.vo.*;
import io.choerodon.devops.api.vo.kubernetes.C7nHelmRelease;
import io.choerodon.devops.api.vo.kubernetes.ImagePullSecret;
import io.choerodon.devops.api.vo.kubernetes.InstanceValueVO;
import io.choerodon.devops.api.vo.kubernetes.Metadata;
import io.choerodon.devops.app.eventhandler.constants.SagaTopicCodeConstants;
import io.choerodon.devops.app.eventhandler.payload.InstanceSagaPayload;
import io.choerodon.devops.app.service.*;
import io.choerodon.devops.infra.dto.*;
import io.choerodon.devops.infra.dto.iam.IamUserDTO;
import io.choerodon.devops.infra.dto.iam.ProjectDTO;
import io.choerodon.devops.infra.enums.*;
import io.choerodon.devops.infra.feign.operator.GitlabServiceClientOperator;
import io.choerodon.devops.infra.feign.operator.BaseServiceClientOperator;
import io.choerodon.devops.infra.gitops.ResourceConvertToYamlHandler;
import io.choerodon.devops.infra.gitops.ResourceFileCheckHandler;
import io.choerodon.devops.infra.handler.ClusterConnectionHandler;
import io.choerodon.devops.infra.mapper.AppServiceInstanceMapper;
import io.choerodon.devops.infra.mapper.DevopsEnvAppServiceMapper;
import io.choerodon.devops.infra.util.*;

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
public class  AppServiceInstanceServiceImpl implements AppServiceInstanceService {

    public static final String CREATE = "create";
    public static final String UPDATE = "update";
    public static final String CHOERODON = "choerodon-test";
    public static final String HARBOR = "harbor";
    public static final String CHART = "chart";
    private static final String YAML_SUFFIX = ".yaml";
    private static final String RELEASE_PREFIX = "release-";
    private static final String FILE_SEPARATOR = "file.separator";
    private static final String C7NHELM_RELEASE = "C7NHelmRelease";
    private static final String RELEASE_NAME = "ReleaseName";
    public static final String APP_SERVICE = "appService";
    private static Gson gson = new Gson();

    @Value("${services.helm.url}")
    private String helmUrl;
    @Value("${services.gitlab.url}")
    private String gitlabUrl;
    @Value("${services.gitlab.sshUrl}")
    private String gitlabSshUrl;

    @Autowired
    private AgentCommandService agentCommandService;
    @Autowired
    private ClusterConnectionHandler clusterConnectionHandler;
    @Autowired
    private AppServiceInstanceMapper appServiceInstanceMapper;
    @Autowired
    private DevopsEnvResourceService devopsEnvResourceService;
    @Autowired
    private DevopsEnvironmentService devopsEnvironmentService;
    @Autowired
    private DevopsEnvUserPermissionService devopsEnvUserPermissionService;
    @Autowired
    private BaseServiceClientOperator baseServiceClientOperator;
    @Autowired
    private AppServiceVersionService appServiceVersionService;
    @Autowired
    private DevopsDeployValueService devopsDeployValueService;
    @Autowired
    private TransactionalProducer producer;
    @Autowired
    private UserAttrService userAttrService;
    @Autowired
    private AppServiceService applicationService;
    @Autowired
    private DevopsConfigService devopsConfigService;
    @Autowired
    private DevopsRegistrySecretService devopsRegistrySecretService;
    @Autowired
    private DevopsEnvCommandService devopsEnvCommandService;
    @Autowired
    private DevopsEnvCommandValueService devopsEnvCommandValueService;
    @Autowired
    private DevopsEnvPodService devopsEnvPodService;
    @Autowired
    private DevopsEnvFileResourceService devopsEnvFileResourceService;
    @Autowired
    private GitlabServiceClientOperator gitlabServiceClientOperator;
    @Autowired
    private PipelineAppDeployService pipelineAppDeployService;
    @Autowired
    private AppServiceVersionValueService appServiceVersionValueService;
    @Autowired
    private AppServiceVersionReadmeService appServiceVersionReadmeService;
    @Autowired
    private ResourceFileCheckHandler resourceFileCheckHandler;
    @Autowired
    private DevopsEnvAppServiceMapper devopsEnvAppServiceMapper;
    @Autowired
    private MarketConnectInfoService marketConnectInfoService;
    @Autowired
    private DevopsIngressService devopsIngressService;
    @Autowired
    private DevopsServiceService devopsServiceService;
    @Autowired
    private IamService iamService;
    @Autowired
    private DevopsDeployRecordService devopsDeployRecordService;

    @Override
    public AppServiceInstanceInfoVO queryInfoById(Long instanceId) {
        AppServiceInstanceInfoDTO appServiceInstanceInfoDTO = appServiceInstanceMapper.queryInfoById(instanceId);
        if (appServiceInstanceInfoDTO == null) {
            return null;
        }
        List<Long> updatedEnv = clusterConnectionHandler.getUpdatedEnvList();
        AppServiceInstanceInfoVO appServiceInstanceInfoVO = new AppServiceInstanceInfoVO();
        BeanUtils.copyProperties(appServiceInstanceInfoDTO, appServiceInstanceInfoVO);
        appServiceInstanceInfoVO.setConnect(updatedEnv.contains(appServiceInstanceInfoDTO.getClusterId()));
        return appServiceInstanceInfoVO;
    }

    @Override
    public PageInfo<AppServiceInstanceInfoVO> pageInstanceInfoByOptions(Long projectId, Long envId, PageRequest pageRequest, String params) {
        Map<String, Object> maps = TypeUtil.castMapParams(params);
        return ConvertUtils.convertPage(PageHelper.startPage(pageRequest.getPage(), pageRequest.getSize(), PageRequestUtil.getOrderBy(pageRequest))
                .doSelectPageInfo(() -> appServiceInstanceMapper.listInstanceInfoByEnvAndOptions(
                        envId, TypeUtil.cast(maps.get(TypeUtil.SEARCH_PARAM)), TypeUtil.cast(maps.get(TypeUtil.PARAMS)))),
                AppServiceInstanceInfoVO.class);
    }

    @Override
    public PageInfo<DevopsEnvPreviewInstanceVO> pageByOptions(Long projectId, PageRequest pageRequest,
                                                              Long envId, Long appServiceVersionId, Long appServiceId, Long instanceId, String params) {

        PageInfo<DevopsEnvPreviewInstanceVO> devopsEnvPreviewInstanceDTOPageInfo = new PageInfo<>();

        Map maps = gson.fromJson(params, Map.class);
        Map<String, Object> searchParamMap = TypeUtil.cast(maps.get(TypeUtil.SEARCH_PARAM));
        List<String> paramList = TypeUtil.cast(maps.get(TypeUtil.PARAMS));
        PageInfo<AppServiceInstanceDTO> applicationInstanceDTOPageInfo = PageHelper.startPage(pageRequest.getPage(), pageRequest.getSize(), PageRequestUtil.getOrderBy(pageRequest)).doSelectPageInfo(() ->
                appServiceInstanceMapper
                        .listApplicationInstance(projectId, envId, appServiceVersionId, appServiceId, instanceId, searchParamMap, paramList));

        BeanUtils.copyProperties(applicationInstanceDTOPageInfo, devopsEnvPreviewInstanceDTOPageInfo);

        return devopsEnvPreviewInstanceDTOPageInfo;

    }


    @Override
    public List<AppServiceInstanceOverViewVO> listApplicationInstanceOverView(Long projectId, Long appServiceId) {


        //查询出当前用户有权限的环境列表，如果是项目所有者，则有全部环境权限
        List<Long> permissionEnvIds = devopsEnvUserPermissionService
                .listByUserId(TypeUtil.objToLong(GitUserNameUtil.getUserId())).stream()
                .filter(DevopsEnvUserPermissionDTO::getPermitted).map(DevopsEnvUserPermissionDTO::getEnvId)
                .collect(Collectors.toList());

        ProjectDTO projectDTO = baseServiceClientOperator.queryIamProjectById(projectId);
        if (baseServiceClientOperator.isProjectOwner(TypeUtil.objToLong(GitUserNameUtil.getUserId()), projectDTO)) {
            permissionEnvIds = devopsEnvironmentService.baseListByProjectId(projectId).stream()
                    .map(DevopsEnvironmentDTO::getId).collect(Collectors.toList());
        }

        List<AppServiceInstanceOverViewDTO> appServiceInstanceOverViewDTOS = baseListApplicationInstanceOverView(projectId, appServiceId,
                permissionEnvIds);

        List<AppServiceLatestVersionDTO> appLatestVersionList = appServiceVersionService.baseListAppNewestVersion(projectId);

        Map<Long, AppServiceLatestVersionDTO> latestVersionList = appLatestVersionList.stream()
                .collect(Collectors.toMap(AppServiceLatestVersionDTO::getAppServiceId, t -> t, (a, b) -> b));

        //查询部署总览，每个应用最新的版本以及在每个环境每个实例部署的版本
        Map<Long, Integer> appServiceInstancesListMap = new HashMap<>();
        List<AppServiceInstanceOverViewVO> appServiceInstanceOverViewVOS = new ArrayList<>();
        appServiceInstanceOverViewDTOS.forEach(t -> {
            AppServiceInstanceOverViewVO appServiceInstanceOverViewVO = new AppServiceInstanceOverViewVO();
            if (appServiceInstancesListMap.get(t.getAppServiceId()) == null) {
                if (t.getInstanceId() != null
                        || t.getVersionId().equals(latestVersionList.get(t.getAppServiceId()).getVersionId())) {
                    appServiceInstanceOverViewVO = new AppServiceInstanceOverViewVO(
                            t.getAppServiceId(),
                            t.getPublishLevel(),
                            t.getAppServiceName(),
                            t.getAppServiceCode(),
                            latestVersionList.get(t.getAppServiceId()).getVersionId(),
                            latestVersionList.get(t.getAppServiceId()).getVersion());
                    appServiceInstanceOverViewVO.setProjectId(t.getProjectId());
                    if (t.getInstanceId() != null) {
                        initInstanceOverView(appServiceInstanceOverViewVO, t, latestVersionList.get(t.getAppServiceId()).getVersionId());
                    }
                    appServiceInstancesListMap.put(t.getAppServiceId(), appServiceInstanceOverViewVOS.size());
                    appServiceInstanceOverViewVOS.add(appServiceInstanceOverViewVO);
                }
            } else {
                appServiceInstanceOverViewVO = appServiceInstanceOverViewVOS.get(appServiceInstancesListMap.get(t.getAppServiceId()));
                initInstanceOverViewIfNotExist(appServiceInstanceOverViewVO, t);
            }
            if (t.getInstanceId() != null
                    && t.getVersion().equalsIgnoreCase(appServiceInstanceOverViewVO.getLatestVersion())) {
                appServiceInstanceOverViewVO.addLatestVersionRunning();
            }
        });
        return appServiceInstanceOverViewVOS;
    }


    @Override
    public InstanceValueVO queryDeployValue(String type, Long instanceId, Long appServiceVersionId) {
        InstanceValueVO instanceValueVO = new InstanceValueVO();
        String versionValue = FileUtil.checkValueFormat(appServiceVersionService.baseQueryValue(appServiceVersionId));

        if (type.equals(UPDATE)) {
            AppServiceInstanceDTO appServiceInstanceDTO = baseQuery(instanceId);
            if (appServiceInstanceDTO.getValueId() != null) {
                DevopsDeployValueDTO devopsDeployValueDTO = devopsDeployValueService.baseQueryById(appServiceInstanceDTO.getValueId());
                instanceValueVO.setName(devopsDeployValueDTO.getName());
                instanceValueVO.setId(devopsDeployValueDTO.getId());
                instanceValueVO.setObjectVersionNumber(devopsDeployValueDTO.getObjectVersionNumber());
            }
            instanceValueVO.setYaml(getReplaceResult(versionValue, baseQueryValueByInstanceId(instanceId)).getYaml());
        } else {
            try {
                FileUtil.checkYamlFormat(versionValue);
            } catch (Exception e) {
                instanceValueVO.setYaml(versionValue);
                return instanceValueVO;
            }
            instanceValueVO.setYaml(versionValue);
        }
        return instanceValueVO;
    }

    @Override
    public InstanceValueVO queryUpgradeValue(Long instanceId, Long appServiceVersionId) {
        AppServiceInstanceDTO appServiceInstanceDTO = baseQuery(instanceId);
        String yaml = FileUtil.checkValueFormat(baseQueryValueByInstanceId(instanceId));
        String versionValue = appServiceVersionService.baseQueryValue(appServiceVersionId);
        InstanceValueVO instanceValueVO = new InstanceValueVO();
        if (appServiceInstanceDTO.getValueId() != null) {
            DevopsDeployValueDTO devopsDeployValueDTO = devopsDeployValueService.baseQueryById(appServiceInstanceDTO.getValueId());
            instanceValueVO.setName(devopsDeployValueDTO.getName());
            instanceValueVO.setId(devopsDeployValueDTO.getId());
            instanceValueVO.setObjectVersionNumber(devopsDeployValueDTO.getObjectVersionNumber());
        }
        instanceValueVO.setYaml(getReplaceResult(versionValue, yaml).getYaml());
        return instanceValueVO;
    }

    @Override
    public DeployTimeVO listDeployTime(Long projectId, Long envId, Long[] appServiceIds,
                                       Date startTime, Date endTime) {

        DeployTimeVO deployTimeVO = new DeployTimeVO();

        if (appServiceIds.length == 0) {
            return deployTimeVO;
        }

        List<DeployDTO> deployDTOS = baseListDeployTime(projectId, envId, appServiceIds, startTime, endTime);
        List<Date> creationDates = deployDTOS.stream().map(DeployDTO::getCreationDate).collect(Collectors.toList());

        //操作时间排序
        creationDates = new ArrayList<>(new HashSet<>(creationDates)).stream().sorted(Comparator.naturalOrder())
                .collect(Collectors.toList());

        List<DeployAppVO> deployAppVOS = new ArrayList<>();

        //以应用为维度分组
        Map<String, List<DeployDTO>> resultMaps = deployDTOS.stream()
                .collect(Collectors.groupingBy(DeployDTO::getAppServiceName));

        resultMaps.forEach((key, value) -> {
            DeployAppVO deployAppVO = new DeployAppVO();
            List<DeployDetailVO> deployDetailVOS = new ArrayList<>();
            deployAppVO.setAppServiceName(key);
            //给应用下每个实例操作设置时长
            value.forEach(deployDO -> {
                DeployDetailVO deployDetailVO = new DeployDetailVO();
                deployDetailVO.setDeployDate(deployDO.getCreationDate());
                deployDetailVO.setDeployTime(
                        getDeployTime(deployDO.getLastUpdateDate().getTime() - deployDO.getCreationDate().getTime()));
                deployDetailVOS.add(deployDetailVO);
            });
            deployAppVO.setDeployDetailVOS(deployDetailVOS);
            deployAppVOS.add(deployAppVO);
        });
        deployTimeVO.setCreationDates(creationDates);
        deployTimeVO.setDeployAppVOS(deployAppVOS);
        return deployTimeVO;
    }


    @Override
    public DeployFrequencyVO listDeployFrequency(Long projectId, Long[] envIds,
                                                 Long appServiceId, Date startTime, Date endTime) {
        if (envIds.length == 0) {
            return new DeployFrequencyVO();
        }
        List<DeployDTO> deployDTOS = baselistDeployFrequency(projectId, envIds, appServiceId, startTime, endTime);

        //以时间维度分组
        Map<String, List<DeployDTO>> resultMaps = deployDTOS.stream()
                .collect(Collectors.groupingBy(t -> new java.sql.Date(t.getCreationDate().getTime()).toString()));

        List<String> creationDates = deployDTOS.stream()
                .map(deployDTO -> new java.sql.Date(deployDTO.getCreationDate().getTime()).toString())
                .collect(Collectors.toList());
        creationDates = new ArrayList<>(new HashSet<>(creationDates)).stream().sorted(Comparator.naturalOrder())
                .collect(Collectors.toList());


        List<Long> deployFrequency = new LinkedList<>();
        List<Long> deploySuccessFrequency = new LinkedList<>();
        List<Long> deployFailFrequency = new LinkedList<>();
        creationDates.forEach(date -> {
            Long[] newDeployFrequency = {0L};
            Long[] newDeploySuccessFrequency = {0L};
            Long[] newDeployFailFrequency = {0L};
            resultMaps.get(date).forEach(deployFrequencyDO -> {
                newDeployFrequency[0] = newDeployFrequency[0] + 1L;
                if (deployFrequencyDO.getStatus().equals(CommandStatus.SUCCESS.getStatus())) {
                    newDeploySuccessFrequency[0] = newDeploySuccessFrequency[0] + 1L;
                } else {
                    newDeployFailFrequency[0] = newDeployFailFrequency[0] + 1L;
                }
            });
            deployFrequency.add(newDeployFrequency[0]);
            deploySuccessFrequency.add(newDeploySuccessFrequency[0]);
            deployFailFrequency.add(newDeployFailFrequency[0]);
        });
        DeployFrequencyVO deployFrequencyVO = new DeployFrequencyVO();
        deployFrequencyVO.setCreationDates(creationDates);
        deployFrequencyVO.setDeployFailFrequency(deployFailFrequency);
        deployFrequencyVO.setDeploySuccessFrequency(deploySuccessFrequency);
        deployFrequencyVO.setDeployFrequencys(deployFrequency);
        return deployFrequencyVO;
    }

    @Override
    public PageInfo<DeployDetailTableVO> pageDeployFrequencyTable(Long projectId, PageRequest pageRequest, Long[] envIds,
                                                                  Long appServiceId, Date startTime, Date endTime) {
        if (envIds.length == 0) {
            return new PageInfo<>();
        }
        PageInfo<DeployDTO> deployDTOPageInfo = basePageDeployFrequencyTable(projectId, pageRequest,
                envIds, appServiceId, startTime, endTime);
        return getDeployDetailDTOS(deployDTOPageInfo);
    }


    @Override
    public PageInfo<DeployDetailTableVO> pageDeployTimeTable(Long projectId, PageRequest pageRequest,
                                                             Long[] appServiceIds, Long envId,
                                                             Date startTime, Date endTime) {
        if (appServiceIds.length == 0) {
            return new PageInfo<>();
        }
        PageInfo<DeployDTO> deployDTOS = basePageDeployTimeTable(projectId, pageRequest, envId,
                appServiceIds, startTime, endTime);
        return getDeployDetailDTOS(deployDTOS);
    }


    @Override
    public void deployTestApp(AppServiceDeployVO appServiceDeployVO) {

        String versionValue = appServiceVersionService.baseQueryValue(appServiceDeployVO.getAppServiceVersionId());
        AppServiceDTO applicationDTO = applicationService.baseQuery(appServiceDeployVO.getAppServiceId());

        DevopsEnvironmentDTO devopsEnvironmentDTO = new DevopsEnvironmentDTO();
        devopsEnvironmentDTO.setCode(CHOERODON);
        devopsEnvironmentDTO.setClusterId(appServiceDeployVO.getEnvironmentId());
        String secretCode = getSecret(applicationDTO, null, devopsEnvironmentDTO);

        AppServiceVersionDTO appServiceVersionDTO = appServiceVersionService.baseQuery(appServiceDeployVO.getAppServiceVersionId());
        FileUtil.checkYamlFormat(appServiceDeployVO.getValues());
        String deployValue = getReplaceResult(versionValue,
                appServiceDeployVO.getValues()).getDeltaYaml().trim();
        agentCommandService.deployTestApp(applicationDTO, appServiceVersionDTO, appServiceDeployVO.getInstanceName(), secretCode, appServiceDeployVO.getEnvironmentId(), deployValue);
    }


    @Override
    public InstanceControllerDetailVO queryInstanceResourceDetailJson(Long instanceId, String resourceName,
                                                                      ResourceType resourceType) {
        String message = getAndCheckResourceDetail(instanceId, resourceName, resourceType);

        try {
            return new InstanceControllerDetailVO(instanceId, new ObjectMapper().readTree(message));
        } catch (IOException e) {
            throw new CommonException("error.instance.resource.json.read.failed", instanceId, message);
        }
    }

    @Override
    public InstanceControllerDetailVO getInstanceResourceDetailYaml(Long instanceId, String resourceName,
                                                                    ResourceType resourceType) {
        String message = getAndCheckResourceDetail(instanceId, resourceName, resourceType);

        try {
            return new InstanceControllerDetailVO(instanceId, JsonYamlConversionUtil.json2yaml(message));
        } catch (IOException e) {
            throw new CommonException(JsonYamlConversionUtil.ERROR_JSON_TO_YAML_FAILED, message);
        }
    }

    @Override
    public void getTestAppStatus(Map<Long, List<String>> testReleases) {
        agentCommandService.getTestAppStatus(testReleases);
    }

    @Override
    public void operationPodCount(String deploymentName, Long envId, Long count) {

        DevopsEnvironmentDTO devopsEnvironmentDTO = devopsEnvironmentService.baseQueryById(envId);

        UserAttrDTO userAttrDTO = userAttrService.baseQueryById(TypeUtil.objToLong(GitUserNameUtil.getUserId()));

        //校验环境相关信息
        devopsEnvironmentService.checkEnv(devopsEnvironmentDTO, userAttrDTO);

        //不能减少到0
        if (count == 0) {
            return;
        }
        agentCommandService.operatePodCount(deploymentName, devopsEnvironmentDTO.getCode(), devopsEnvironmentDTO.getClusterId(), count);
    }


    @Override
    public InstanceValueVO queryLastDeployValue(Long instanceId) {
        InstanceValueVO instanceValueVO = new InstanceValueVO();
        String yaml = FileUtil.checkValueFormat(baseQueryValueByInstanceId(
                instanceId));
        instanceValueVO.setYaml(yaml);
        return instanceValueVO;
    }

    @Override
    public List<ErrorLineVO> formatValue(InstanceValueVO instanceValueVO) {
        try {
            FileUtil.checkYamlFormat(instanceValueVO.getYaml());

            String fileName = GenerateUUID.generateUUID() + YAML_SUFFIX;
            String path = "deployfile";
            FileUtil.saveDataToFile(path, fileName, instanceValueVO.getYaml());
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
    public DevopsEnvPreviewVO listByEnv(Long projectId, Long envId, String params) {

        Map<String, Object> maps = gson.fromJson(params, new TypeToken<Map<String, Object>>() {
        }.getType());


        Map<String, Object> searchParamMap = TypeUtil.cast(maps.get(TypeUtil.SEARCH_PARAM));

        List<AppServiceInstanceDTO> appServiceInstanceDTOS = appServiceInstanceMapper
                .listApplicationInstance(projectId, envId, null, null, null, searchParamMap, TypeUtil.cast(maps.get(TypeUtil.PARAMS)));

        List<AppServiceInstanceVO> appServiceInstanceVOS =
                ConvertUtils.convertList(appServiceInstanceDTOS, AppServiceInstanceVO.class);

        //以app为维度给实例分组
        Map<Long, List<AppServiceInstanceVO>> resultMaps = appServiceInstanceVOS.stream()
                .collect(Collectors.groupingBy(AppServiceInstanceVO::getAppServiceId));
        DevopsEnvPreviewVO devopsEnvPreviewVO = new DevopsEnvPreviewVO();
        List<DevopsEnvPreviewAppVO> devopsEnvPreviewAppVOS = new ArrayList<>();
        resultMaps.forEach((key, value) -> {
            DevopsEnvPreviewAppVO devopsEnvPreviewAppVO = new DevopsEnvPreviewAppVO();
            devopsEnvPreviewAppVO.setAppServiceName(value.get(0).getAppServiceName());
            devopsEnvPreviewAppVO.setAppServiceCode(value.get(0).getAppServiceCode());
            devopsEnvPreviewAppVO.setProjectId(value.get(0).getProjectId());

            //设置应用所属的实例
            devopsEnvPreviewAppVO.setAppServiceInstanceVOS(appServiceInstanceVOS);

            devopsEnvPreviewAppVOS.add(devopsEnvPreviewAppVO);
        });
        devopsEnvPreviewVO.setDevopsEnvPreviewAppVOS(devopsEnvPreviewAppVOS);
        return devopsEnvPreviewVO;
    }


    @Override
    public DevopsEnvResourceVO listResourcesInHelmRelease(Long instanceId) {

        // 获取相关的pod
        List<DevopsEnvPodVO> devopsEnvPodDTOS = ConvertUtils.convertList(devopsEnvPodService.baseListByInstanceId(instanceId), DevopsEnvPodVO.class);

        DevopsEnvResourceVO devopsEnvResourceVO = devopsEnvResourceService
                .listResourcesInHelmRelease(instanceId);

        // 关联其pod并设置deployment
        devopsEnvResourceVO.setDeploymentVOS(devopsEnvResourceVO.getDeploymentVOS()
                .stream()
                .peek(deploymentVO -> deploymentVO.setDevopsEnvPodVOS(filterPodsAssociated(devopsEnvPodDTOS, deploymentVO.getName())))
                .collect(Collectors.toList())
        );

        // 关联其pod并设置daemonSet
        devopsEnvResourceVO.setDaemonSetVOS(
                devopsEnvResourceVO.getDaemonSetVOS()
                        .stream()
                        .peek(daemonSetVO -> daemonSetVO.setDevopsEnvPodDTOS(
                                filterPodsAssociatedWithDaemonSet(devopsEnvPodDTOS, daemonSetVO.getName())
                        ))
                        .collect(Collectors.toList())
        );

        // 关联其pod并设置statefulSet
        devopsEnvResourceVO.setStatefulSetVOS(
                devopsEnvResourceVO.getStatefulSetVOS()
                        .stream()
                        .peek(statefulSetVO -> statefulSetVO.setDevopsEnvPodVOS(
                                filterPodsAssociatedWithStatefulSet(devopsEnvPodDTOS, statefulSetVO.getName()))
                        )
                        .collect(Collectors.toList())
        );


        return devopsEnvResourceVO;
    }

    @Override
    @Saga(code = SagaTopicCodeConstants.DEVOPS_CREATE_INSTANCE,
            description = "Devops创建实例", inputSchema = "{}")
    @Transactional(rollbackFor = Exception.class)
    public AppServiceInstanceVO createOrUpdate(AppServiceDeployVO appServiceDeployVO) {

        DevopsEnvironmentDTO devopsEnvironmentDTO = devopsEnvironmentService.baseQueryById(appServiceDeployVO.getEnvironmentId());

        UserAttrDTO userAttrDTO = userAttrService.baseQueryById(TypeUtil.objToLong(GitUserNameUtil.getUserId()));

        //校验环境相关信息
        devopsEnvironmentService.checkEnv(devopsEnvironmentDTO, userAttrDTO);

        //校验values
        FileUtil.checkYamlFormat(appServiceDeployVO.getValues());


        AppServiceDTO applicationDTO = applicationService.baseQuery(appServiceDeployVO.getAppServiceId());
        AppServiceVersionDTO appServiceVersionDTO =
                appServiceVersionService.baseQuery(appServiceDeployVO.getAppServiceVersionId());

        //初始化ApplicationInstanceDTO,DevopsEnvCommandDTO,DevopsEnvCommandValueDTO
        AppServiceInstanceDTO appServiceInstanceDTO = initApplicationInstanceDTO(appServiceDeployVO);
        DevopsEnvCommandDTO devopsEnvCommandDTO = initDevopsEnvCommandDTO(appServiceDeployVO);
        DevopsEnvCommandValueDTO devopsEnvCommandValueDTO = initDevopsEnvCommandValueDTO(appServiceDeployVO);

        String secretCode = null;
        //获取部署实例时授权secret的code
        secretCode = getSecret(applicationDTO, secretCode, devopsEnvironmentDTO);

        // 初始化自定义实例名
        String code;
        if (appServiceDeployVO.getType().equals(CREATE)) {
            if (appServiceDeployVO.getInstanceName() == null || appServiceDeployVO.getInstanceName().trim().equals("")) {
                code = String.format("%s-%s", applicationDTO.getCode(), GenerateUUID.generateUUID().substring(0, 5));
            } else {
                code = appServiceDeployVO.getInstanceName();
            }
        } else {
            code = appServiceInstanceDTO.getCode();
            //更新实例的时候校验gitops库文件是否存在,处理部署实例时，由于没有创gitops文件导致的部署失败
            resourceFileCheckHandler.check(devopsEnvironmentDTO, appServiceDeployVO.getInstanceId(), code, C7NHELM_RELEASE);

            //从未关联部署配置到关联部署配置，或者从一个部署配置关联另外一个部署配置，如果values是一样的，虽然getIsNotChange为false,但是此时也应该直接设置为isNotChange为true
            DevopsEnvCommandDTO oldDevopsEnvCommandE = devopsEnvCommandService.baseQuery(baseQuery(appServiceInstanceDTO.getId()).getCommandId());
            String deployValue = baseQueryValueByInstanceId(appServiceInstanceDTO.getId());
            if (appServiceDeployVO.getAppServiceVersionId().equals(oldDevopsEnvCommandE.getObjectVersionId()) && deployValue.equals(appServiceDeployVO.getValues())) {
                appServiceDeployVO.setIsNotChange(true);
            }
        }

        //更新时候，如果isNotChange的值为true，则直接向agent发送更新指令，不走gitops,否则走操作gitops库文件逻辑
        if (appServiceDeployVO.getIsNotChange()) {
            appServiceInstanceDTO = restartDeploy(devopsEnvironmentDTO, applicationDTO, appServiceVersionDTO, appServiceInstanceDTO, devopsEnvCommandValueDTO, secretCode);
        } else {
            //存储数据
            if (appServiceDeployVO.getType().equals(CREATE)) {
                createEnvAppRelationShipIfNon(appServiceDeployVO.getAppServiceId(), appServiceDeployVO.getEnvironmentId());
                appServiceInstanceDTO.setCode(code);
                appServiceInstanceDTO.setId(baseCreate(appServiceInstanceDTO).getId());
                devopsEnvCommandDTO.setObjectId(appServiceInstanceDTO.getId());
                devopsEnvCommandDTO.setValueId(devopsEnvCommandValueService.baseCreate(devopsEnvCommandValueDTO).getId());
                appServiceInstanceDTO.setCommandId(devopsEnvCommandService.baseCreate(devopsEnvCommandDTO).getId());
                baseUpdate(appServiceInstanceDTO);
            } else {
                devopsEnvCommandDTO.setObjectId(appServiceInstanceDTO.getId());
                devopsEnvCommandDTO.setValueId(devopsEnvCommandValueService.baseCreate(devopsEnvCommandValueDTO).getId());
                appServiceInstanceDTO.setCommandId(devopsEnvCommandService.baseCreate(devopsEnvCommandDTO).getId());
                baseUpdate(appServiceInstanceDTO);
            }
        }

        //插入部署记录
        DevopsDeployRecordDTO devopsDeployRecordDTO = new DevopsDeployRecordDTO(devopsEnvironmentDTO.getProjectId(), "manual", devopsEnvCommandDTO.getId(), devopsEnvironmentDTO.getId().toString(), devopsEnvCommandDTO.getCreationDate());
        devopsDeployRecordService.baseCreate(devopsDeployRecordDTO);

        appServiceDeployVO.setInstanceId(appServiceInstanceDTO.getId());
        appServiceDeployVO.setInstanceName(code);
        InstanceSagaPayload instanceSagaPayload = new InstanceSagaPayload(applicationDTO.getAppId(), userAttrDTO.getGitlabUserId(), secretCode);
        instanceSagaPayload.setApplicationDTO(applicationDTO);
        instanceSagaPayload.setAppServiceVersionDTO(appServiceVersionDTO);
        instanceSagaPayload.setAppServiceDeployVO(appServiceDeployVO);
        instanceSagaPayload.setDevopsEnvironmentDTO(devopsEnvironmentDTO);


        producer.apply(
                StartSagaBuilder
                        .newBuilder()
                        .withLevel(ResourceLevel.PROJECT)
                        .withRefType("env")
                        .withSagaCode(SagaTopicCodeConstants.DEVOPS_CREATE_INSTANCE),
                builder -> builder
                        .withPayloadAndSerialize(instanceSagaPayload)
                        .withRefId(devopsEnvironmentDTO.getId().toString()));

        //如果部署时，也指定了创建网络和域名
        if (appServiceDeployVO.getDevopsServiceReqVO() != null) {
            devopsServiceService.create(devopsEnvironmentDTO.getProjectId(), appServiceDeployVO.getDevopsServiceReqVO());
        }
        if (appServiceDeployVO.getDevopsIngressVO() != null) {
            devopsIngressService.createIngress(devopsEnvironmentDTO.getProjectId(), appServiceDeployVO.getDevopsIngressVO());
        }


        return ConvertUtils.convertObject(appServiceInstanceDTO, AppServiceInstanceVO.class);
    }

    /**
     * 为环境和应用创建关联关系如果不存在
     *
     * @param appServiceId 应用id
     * @param envId 环境id
     */
    private void createEnvAppRelationShipIfNon(Long appServiceId, Long envId) {
        DevopsEnvAppServiceDTO devopsEnvAppServiceDTO = new DevopsEnvAppServiceDTO();
        devopsEnvAppServiceDTO.setAppServiceId(appServiceId);
        devopsEnvAppServiceDTO.setEnvId(envId);
        devopsEnvAppServiceMapper.insertIgnore(devopsEnvAppServiceDTO);
    }

    @Override
    public void createInstanceBySaga(InstanceSagaPayload instanceSagaPayload) {

        try {
            //判断当前容器目录下是否存在环境对应的gitops文件目录，不存在则克隆
            String filePath = clusterConnectionHandler.handDevopsEnvGitRepository(instanceSagaPayload.getProjectId(), instanceSagaPayload.getDevopsEnvironmentDTO().getCode(), instanceSagaPayload.getDevopsEnvironmentDTO().getEnvIdRsa());

            //在gitops库处理instance文件
            ResourceConvertToYamlHandler<C7nHelmRelease> resourceConvertToYamlHandler = new ResourceConvertToYamlHandler<>();
            resourceConvertToYamlHandler.setType(getC7NHelmRelease(
                    instanceSagaPayload.getAppServiceDeployVO().getInstanceName(), instanceSagaPayload.getAppServiceVersionDTO().getRepository(), instanceSagaPayload.getApplicationDTO().getCode(), instanceSagaPayload.getAppServiceVersionDTO().getVersion(), instanceSagaPayload.getAppServiceDeployVO().getValues(), instanceSagaPayload.getAppServiceDeployVO().getAppServiceVersionId(), instanceSagaPayload.getSecretCode()));

            resourceConvertToYamlHandler.operationEnvGitlabFile(
                    RELEASE_PREFIX + instanceSagaPayload.getAppServiceDeployVO().getInstanceName(),
                    instanceSagaPayload.getDevopsEnvironmentDTO().getGitlabEnvProjectId().intValue(),
                    instanceSagaPayload.getAppServiceDeployVO().getType(),
                    instanceSagaPayload.getGitlabUserId(),
                    instanceSagaPayload.getAppServiceDeployVO().getInstanceId(), C7NHELM_RELEASE, null, false, instanceSagaPayload.getDevopsEnvironmentDTO().getId(), filePath);
        } catch (Exception e) {
            //有异常更新实例以及command的状态
            AppServiceInstanceDTO appServiceInstanceDTO = baseQuery(instanceSagaPayload.getAppServiceDeployVO().getInstanceId());
            appServiceInstanceDTO.setStatus(CommandStatus.FAILED.getStatus());
            baseUpdate(appServiceInstanceDTO);
            DevopsEnvCommandDTO devopsEnvCommandDTO = devopsEnvCommandService.baseQuery(appServiceInstanceDTO.getCommandId());
            devopsEnvCommandDTO.setStatus(CommandStatus.FAILED.getStatus());
            devopsEnvCommandDTO.setError("create or update gitOps file failed!");
            devopsEnvCommandService.baseUpdate(devopsEnvCommandDTO);
        }
    }

    @Override
    public AppServiceInstanceRepVO queryByCommandId(Long commandId) {
        DevopsEnvCommandDTO devopsEnvCommandDTO = devopsEnvCommandService.baseQuery(commandId);
        AppServiceInstanceDTO appServiceInstanceDTO = baseQuery(devopsEnvCommandDTO.getObjectId());
        AppServiceInstanceRepVO appServiceInstanceRepVO = new AppServiceInstanceRepVO();
        appServiceInstanceRepVO.setAppServiceName(applicationService.baseQuery(appServiceInstanceDTO.getAppServiceId()).getName());
        appServiceInstanceRepVO.setAppServiceVersion(appServiceVersionService.baseQuery(devopsEnvCommandDTO.getObjectVersionId()).getVersion());
        appServiceInstanceRepVO.setEnvName(devopsEnvironmentService.baseQueryById(appServiceInstanceDTO.getEnvId()).getName());
        appServiceInstanceRepVO.setInstanceName(appServiceInstanceDTO.getCode());
        appServiceInstanceRepVO.setInstanceId(appServiceInstanceDTO.getId());
        return appServiceInstanceRepVO;
    }


    @Override
    public AppServiceInstanceVO createOrUpdateByGitOps(AppServiceDeployVO appServiceDeployVO, Long userId) {
        DevopsEnvironmentDTO devopsEnvironmentDTO = devopsEnvironmentService.baseQueryById(appServiceDeployVO.getEnvironmentId());
        //校验环境是否连接
        clusterConnectionHandler.checkEnvConnection(devopsEnvironmentDTO.getClusterId());

        //校验values
        FileUtil.checkYamlFormat(appServiceDeployVO.getValues());

        //初始化ApplicationInstanceDTO,DevopsEnvCommandDTO,DevopsEnvCommandValueDTO
        AppServiceInstanceDTO appServiceInstanceDTO = initApplicationInstanceDTO(appServiceDeployVO);
        DevopsEnvCommandDTO devopsEnvCommandDTO = initDevopsEnvCommandDTO(appServiceDeployVO);
        DevopsEnvCommandValueDTO devopsEnvCommandValueDTO = initDevopsEnvCommandValueDTO(appServiceDeployVO);

        //实例相关对象数据库操作
        if (appServiceDeployVO.getType().equals(CREATE)) {
            appServiceInstanceDTO.setCode(appServiceDeployVO.getInstanceName());
            appServiceInstanceDTO.setId(baseCreate(appServiceInstanceDTO).getId());
        } else {
            baseUpdate(appServiceInstanceDTO);
        }
        devopsEnvCommandDTO.setCreatedBy(userId);
        devopsEnvCommandDTO.setObjectId(appServiceInstanceDTO.getId());
        devopsEnvCommandDTO.setValueId(devopsEnvCommandValueService.baseCreate(devopsEnvCommandValueDTO).getId());
        appServiceInstanceDTO.setCommandId(devopsEnvCommandService.baseCreate(devopsEnvCommandDTO).getId());
        baseUpdate(appServiceInstanceDTO);


        //插入部署记录
        DevopsDeployRecordDTO devopsDeployRecordDTO = new DevopsDeployRecordDTO(devopsEnvironmentDTO.getProjectId(), "manual", devopsEnvCommandDTO.getId(), devopsEnvironmentDTO.getId().toString(), devopsEnvCommandDTO.getCreationDate());
        devopsDeployRecordService.baseCreate(devopsDeployRecordDTO);


        return ConvertUtils.convertObject(appServiceInstanceDTO, AppServiceInstanceVO.class);
    }


    @Override
    public List<RunningInstanceVO> listRunningInstance(Long projectId, Long appServiceId, Long appServiceVersionId, Long envId) {
        return ConvertUtils.convertList(baseListByOptions(projectId, appServiceId, appServiceVersionId, envId), RunningInstanceVO.class);
    }

    @Override
    public List<RunningInstanceVO> listByAppIdAndEnvId(Long projectId, Long appServiceId, Long envId) {
        return ConvertUtils.convertList(baseListByAppIdAndEnvId(projectId, appServiceId, envId), RunningInstanceVO.class);
    }


    @Override
    public void stopInstance(Long instanceId) {
        handleStartOrStopInstance(instanceId, CommandType.STOP.getType());
    }

    @Override
    public void startInstance(Long instanceId) {
        handleStartOrStopInstance(instanceId, CommandType.RESTART.getType());
    }


    @Override
    public void restartInstance(Long instanceId) {
        AppServiceInstanceDTO appServiceInstanceDTO = baseQuery(instanceId);

        DevopsEnvironmentDTO devopsEnvironmentDTO = devopsEnvironmentService.baseQueryById(appServiceInstanceDTO.getEnvId());

        UserAttrDTO userAttrDTO = userAttrService.baseQueryById(TypeUtil.objToLong(GitUserNameUtil.getUserId()));

        //校验环境相关信息
        devopsEnvironmentService.checkEnv(devopsEnvironmentDTO, userAttrDTO);

        DevopsEnvCommandDTO devopsEnvCommandDTO = devopsEnvCommandService.baseQuery(appServiceInstanceDTO.getCommandId());
        AppServiceDTO applicationDTO = applicationService.baseQuery(appServiceInstanceDTO.getAppServiceId());
        AppServiceVersionDTO appServiceVersionDTO = appServiceVersionService
                .baseQuery(devopsEnvCommandDTO.getObjectVersionId());

        String value = baseQueryValueByInstanceId(instanceId);

        devopsEnvCommandDTO.setId(null);
        devopsEnvCommandDTO.setCommandType(CommandType.UPDATE.getType());
        devopsEnvCommandDTO.setStatus(CommandStatus.OPERATING.getStatus());
        devopsEnvCommandDTO = devopsEnvCommandService.baseCreate(devopsEnvCommandDTO);

        updateInstanceStatus(instanceId, devopsEnvCommandDTO.getId(), InstanceStatus.OPERATING.getStatus());

        //获取授权secret
        String secretCode = getSecret(applicationDTO, null, devopsEnvironmentDTO);

        //发送实例重新部署的command
        agentCommandService.deploy(applicationDTO, appServiceVersionDTO, appServiceInstanceDTO.getCode(), devopsEnvironmentDTO, value, devopsEnvCommandDTO.getId(), secretCode);
    }


    @Override
    @Transactional(rollbackFor = Exception.class)

    public void deleteInstance(Long instanceId) {

        AppServiceInstanceDTO appServiceInstanceDTO = baseQuery(instanceId);

        DevopsEnvironmentDTO devopsEnvironmentDTO = devopsEnvironmentService.baseQueryById(appServiceInstanceDTO.getEnvId());

        UserAttrDTO userAttrDTO = userAttrService.baseQueryById(TypeUtil.objToLong(GitUserNameUtil.getUserId()));

        //校验环境相关信息
        devopsEnvironmentService.checkEnv(devopsEnvironmentDTO, userAttrDTO);

        DevopsEnvCommandDTO devopsEnvCommandDTO = devopsEnvCommandService.baseQuery(appServiceInstanceDTO.getCommandId());
        devopsEnvCommandDTO.setCommandType(CommandType.DELETE.getType());
        devopsEnvCommandDTO.setStatus(CommandStatus.OPERATING.getStatus());
        devopsEnvCommandDTO.setId(null);
        devopsEnvCommandDTO = devopsEnvCommandService.baseCreate(devopsEnvCommandDTO);

        updateInstanceStatus(instanceId, devopsEnvCommandDTO.getId(), InstanceStatus.OPERATING.getStatus());

        pipelineAppDeployService.baseUpdateWithInstanceId(instanceId);


        //判断当前容器目录下是否存在环境对应的gitops文件目录，不存在则克隆
        String path = clusterConnectionHandler.handDevopsEnvGitRepository(devopsEnvironmentDTO.getProjectId(), devopsEnvironmentDTO.getCode(), devopsEnvironmentDTO.getEnvIdRsa());

        DevopsEnvFileResourceDTO devopsEnvFileResourceDTO = devopsEnvFileResourceService
                .baseQueryByEnvIdAndResourceId(devopsEnvironmentDTO.getId(), instanceId, C7NHELM_RELEASE);

        //如果文件对象对应关系不存在，证明没有部署成功，删掉gitops文件,删掉资源
        if (devopsEnvFileResourceDTO == null) {
            baseDelete(instanceId);
            baseDeleteInstanceRelInfo(instanceId);
            if (gitlabServiceClientOperator.getFile(TypeUtil.objToInteger(devopsEnvironmentDTO.getGitlabEnvProjectId()), "master",
                    RELEASE_PREFIX + appServiceInstanceDTO.getCode() + YAML_SUFFIX)) {
                gitlabServiceClientOperator.deleteFile(
                        TypeUtil.objToInteger(devopsEnvironmentDTO.getGitlabEnvProjectId()),
                        RELEASE_PREFIX + appServiceInstanceDTO.getCode() + YAML_SUFFIX,
                        "DELETE FILE",
                        TypeUtil.objToInteger(userAttrDTO.getGitlabUserId()));
            }
            return;
        } else {
            //如果文件对象对应关系存在，但是gitops文件不存在，也直接删掉资源
            if (!gitlabServiceClientOperator.getFile(TypeUtil.objToInteger(devopsEnvironmentDTO.getGitlabEnvProjectId()), "master",
                    devopsEnvFileResourceDTO.getFilePath())) {
                baseDelete(instanceId);
                baseDeleteInstanceRelInfo(instanceId);
                devopsEnvFileResourceService.baseDeleteById(devopsEnvFileResourceDTO.getId());
                return;
            }
        }

        //如果对象所在文件只有一个对象，则直接删除文件,否则把对象从文件中去掉，更新文件
        List<DevopsEnvFileResourceDTO> devopsEnvFileResourceES = devopsEnvFileResourceService
                .baseQueryByEnvIdAndPath(devopsEnvironmentDTO.getId(), devopsEnvFileResourceDTO.getFilePath());
        if (devopsEnvFileResourceES.size() == 1) {
            if (gitlabServiceClientOperator.getFile(TypeUtil.objToInteger(devopsEnvironmentDTO.getGitlabEnvProjectId()), "master",
                    devopsEnvFileResourceDTO.getFilePath())) {
                gitlabServiceClientOperator.deleteFile(
                        TypeUtil.objToInteger(devopsEnvironmentDTO.getGitlabEnvProjectId()),
                        devopsEnvFileResourceDTO.getFilePath(),
                        "DELETE FILE",
                        TypeUtil.objToInteger(userAttrDTO.getGitlabUserId()));
            }
        } else {
            ResourceConvertToYamlHandler<C7nHelmRelease> resourceConvertToYamlHandler = new ResourceConvertToYamlHandler<>();
            C7nHelmRelease c7nHelmRelease = new C7nHelmRelease();
            Metadata metadata = new Metadata();
            metadata.setName(appServiceInstanceDTO.getCode());
            c7nHelmRelease.setMetadata(metadata);
            resourceConvertToYamlHandler.setType(c7nHelmRelease);
            Integer projectId = TypeUtil.objToInteger(devopsEnvironmentDTO.getGitlabEnvProjectId());
            resourceConvertToYamlHandler.operationEnvGitlabFile(
                    RELEASE_PREFIX + appServiceInstanceDTO.getCode(),
                    projectId,
                    "delete",
                    userAttrDTO.getGitlabUserId(),
                    appServiceInstanceDTO.getId(), C7NHELM_RELEASE, null, false, devopsEnvironmentDTO.getId(), path);
        }
    }


    @Override
    public InstanceValueVO queryPreviewValues(InstanceValueVO previewInstanceValueVO, Long appServiceVersionId) {
        String versionValue = appServiceVersionService.baseQueryValue(appServiceVersionId);
        try {
            FileUtil.checkYamlFormat(previewInstanceValueVO.getYaml());
        } catch (Exception e) {
            throw new CommonException(e.getMessage(), e);
        }
        return getReplaceResult(versionValue, previewInstanceValueVO.getYaml());
    }

    @Override
    public void instanceDeleteByGitOps(Long instanceId) {
        AppServiceInstanceDTO instanceDTO = baseQuery(instanceId);

        DevopsEnvironmentDTO devopsEnvironmentDTO = devopsEnvironmentService
                .baseQueryById(instanceDTO.getEnvId());

        //校验环境是否连接
        clusterConnectionHandler.checkEnvConnection(devopsEnvironmentDTO.getClusterId());

        baseDeleteInstanceRelInfo(instanceId);
        baseDelete(instanceId);
    }


    @Override
    public void checkName(String code, Long envId) {

        AppInstanceValidator.checkName(code);

        DevopsEnvironmentDTO devopsEnvironmentDTO = devopsEnvironmentService.baseQueryById(envId);
        List<Long> envIds = devopsEnvironmentService.baseListByClusterId(devopsEnvironmentDTO.getClusterId()).stream().map(DevopsEnvironmentDTO::getId).collect(Collectors.toList());
        if(appServiceInstanceMapper.checkCodeExist(code,envIds)) {
            throw new CommonException("error.app.instance.name.already.exist");
        }
        pipelineAppDeployService.baseCheckName(code, envId);
    }


    @Override
    public InstanceValueVO getReplaceResult(String versionValue, String deployValue) {
        if (versionValue.equals(deployValue) || deployValue.equals("")) {
            InstanceValueVO instanceValueVO = new InstanceValueVO();
            instanceValueVO.setDeltaYaml("");
            instanceValueVO.setYaml(versionValue);
            instanceValueVO.setHighlightMarkers(new ArrayList<>());
            instanceValueVO.setNewLines(new ArrayList<>());
            return instanceValueVO;
        }

        String fileName = GenerateUUID.generateUUID() + YAML_SUFFIX;
        String path = "deployfile";
        FileUtil.saveDataToFile(path, fileName, versionValue + "\n" + "---" + "\n" + deployValue);
        InstanceValueVO instanceValueVO;
        try {
            instanceValueVO = FileUtil.replaceNew(path + System.getProperty(FILE_SEPARATOR) + fileName);
        } catch (Exception e) {
            throw new CommonException(e.getMessage(), e);
        }
        if (instanceValueVO.getHighlightMarkers() == null) {
            instanceValueVO.setHighlightMarkers(new ArrayList<>());
        }
        instanceValueVO.setTotalLine(FileUtil.getFileTotalLine(instanceValueVO.getYaml()));
        FileUtil.deleteFile(path + System.getProperty(FILE_SEPARATOR) + fileName);
        return instanceValueVO;
    }

    @Override
    public AppServiceInstanceDTO baseQueryByCodeAndEnv(String code, Long envId) {
        AppServiceInstanceDTO appServiceInstanceDTO = new AppServiceInstanceDTO();
        appServiceInstanceDTO.setCode(code);
        appServiceInstanceDTO.setEnvId(envId);
        return appServiceInstanceMapper.selectOne(appServiceInstanceDTO);
    }

    public AppServiceInstanceDTO baseCreate(AppServiceInstanceDTO appServiceInstanceDTO) {
        if (appServiceInstanceMapper.insert(appServiceInstanceDTO) != 1) {
            throw new CommonException("error.application.instance.create");
        }
        return appServiceInstanceDTO;
    }

    public AppServiceInstanceDTO baseQuery(Long id) {
        return appServiceInstanceMapper.selectByPrimaryKey(id);
    }

    public List<AppServiceInstanceDTO> baseListByOptions(Long projectId, Long appServiceId, Long appServiceVersionId, Long envId) {
        return appServiceInstanceMapper.listApplicationInstanceCode(
                projectId, envId, appServiceVersionId, appServiceId);
    }

    public List<AppServiceInstanceDTO> baseListByAppIdAndEnvId(Long projectId, Long appServiceId, Long envId) {
        return appServiceInstanceMapper.listRunningAndFailedInstance(
                projectId, envId, appServiceId);
    }

    public int baseCountByOptions(Long envId, Long appServiceId, String appServiceInstanceCode) {
        return appServiceInstanceMapper.countByOptions(envId, appServiceId, appServiceInstanceCode);
    }

    public String baseQueryValueByEnvIdAndAppId(Long
                                                        envId, Long appServiceId) {
        return appServiceInstanceMapper.queryValueByEnvIdAndAppId(envId, appServiceId);
    }

    public void baseUpdate(AppServiceInstanceDTO appServiceInstanceDTO) {
        appServiceInstanceDTO.setObjectVersionNumber(
                appServiceInstanceMapper.selectByPrimaryKey(appServiceInstanceDTO.getId()).getObjectVersionNumber());
        if (appServiceInstanceMapper.updateByPrimaryKeySelective(appServiceInstanceDTO) != 1) {
            throw new CommonException("error.instance.update");
        }
    }


    @Override
    public List<AppServiceInstanceDTO> baseListByEnvId(Long envId) {
        AppServiceInstanceDTO appServiceInstanceDTO = new AppServiceInstanceDTO();
        appServiceInstanceDTO.setEnvId(envId);
        return appServiceInstanceMapper
                .select(appServiceInstanceDTO);
    }

    public List<AppServiceInstanceOverViewDTO> baseListApplicationInstanceOverView(Long projectId, Long appServiceId, List<Long> envIds) {
        if (envIds != null && envIds.isEmpty()) {
            envIds = null;
        }
        return appServiceInstanceMapper.listApplicationInstanceOverView(projectId, appServiceId, envIds);
    }

    @Override
    public List<AppServiceInstanceDTO> baseList() {
        return appServiceInstanceMapper.selectAll();
    }

    @Override
    public String baseQueryValueByInstanceId(Long instanceId) {
        return appServiceInstanceMapper.queryByInstanceId(instanceId);
    }

    public void baseDelete(Long id) {
        appServiceInstanceMapper.deleteByPrimaryKey(id);
    }


    public List<DeployDTO> baseListDeployTime(Long projectId, Long envId, Long[] appServiceIds, Date startTime, Date endTime) {
        return appServiceInstanceMapper
                .listDeployTime(projectId, envId, appServiceIds, new java.sql.Date(startTime.getTime()),
                        new java.sql.Date(endTime.getTime()));
    }

    public List<DeployDTO> baselistDeployFrequency(Long projectId, Long[] envIds, Long appServiceId,
                                                   Date startTime, Date endTime) {
        return appServiceInstanceMapper
                .listDeployFrequency(projectId, envIds, appServiceId, new java.sql.Date(startTime.getTime()),
                        new java.sql.Date(endTime.getTime()));
    }

    public PageInfo<DeployDTO> basePageDeployFrequencyTable(Long projectId, PageRequest pageRequest, Long[] envIds, Long appServiceId,
                                                            Date startTime, Date endTime) {
        return PageHelper.startPage(pageRequest.getPage(), pageRequest.getSize(), PageRequestUtil.getOrderBy(pageRequest)).doSelectPageInfo(() ->
                appServiceInstanceMapper
                        .listDeployFrequency(projectId, envIds, appServiceId, new java.sql.Date(startTime.getTime()),
                                new java.sql.Date(endTime.getTime())));
    }

    public PageInfo<DeployDTO> basePageDeployTimeTable(Long projectId, PageRequest pageRequest, Long envId, Long[] appServiceIds,
                                                       Date startTime, Date endTime) {
        return PageHelper.startPage(pageRequest.getPage(), pageRequest.getSize(), PageRequestUtil.getOrderBy(pageRequest)).doSelectPageInfo(() ->
                appServiceInstanceMapper
                        .listDeployTime(projectId, envId, appServiceIds, new java.sql.Date(startTime.getTime()),
                                new java.sql.Date(endTime.getTime())));
    }

    @Override
    public List<AppServiceInstanceDTO> baseListByAppId(Long appServiceId) {
        AppServiceInstanceDTO appServiceInstanceDTO = new AppServiceInstanceDTO();
        appServiceInstanceDTO.setAppServiceId(appServiceId);
        return appServiceInstanceMapper.select(appServiceInstanceDTO);
    }

    @Override
    public void deleteByEnvId(Long envId) {
        AppServiceInstanceDTO appServiceInstanceDTO = new AppServiceInstanceDTO();
        appServiceInstanceDTO.setEnvId(envId);
        appServiceInstanceMapper.delete(appServiceInstanceDTO);
    }

    @Override
    public List<AppServiceInstanceDTO> baseListByValueId(Long valueId) {
        AppServiceInstanceDTO appServiceInstanceDTO = new AppServiceInstanceDTO();
        appServiceInstanceDTO.setValueId(valueId);
        return appServiceInstanceMapper.select(appServiceInstanceDTO);
    }




    public String baseGetInstanceResourceDetailJson(Long instanceId, String resourceName, ResourceType resourceType) {
        return appServiceInstanceMapper.getInstanceResourceDetailJson(instanceId, resourceName, resourceType.getType());
    }

    public void baseDeleteInstanceRelInfo(Long instanceId) {
        appServiceInstanceMapper.deleteInstanceRelInfo(instanceId);
    }


    private void handleStartOrStopInstance(Long instanceId, String type) {

        AppServiceInstanceDTO appServiceInstanceDTO = baseQuery(instanceId);

        DevopsEnvironmentDTO devopsEnvironmentDTO = devopsEnvironmentService.baseQueryById(appServiceInstanceDTO.getEnvId());

        UserAttrDTO userAttrDTO = userAttrService.baseQueryById(TypeUtil.objToLong(GitUserNameUtil.getUserId()));

        //校验环境相关信息
        devopsEnvironmentService.checkEnv(devopsEnvironmentDTO, userAttrDTO);

        if (CommandType.RESTART.getType().equals(type)) {
            if (!appServiceInstanceDTO.getStatus().equals(InstanceStatus.STOPPED.getStatus())) {
                throw new CommonException("error.instance.not.stop");
            }
        } else {
            if (!appServiceInstanceDTO.getStatus().equals(InstanceStatus.RUNNING.getStatus())) {
                throw new CommonException("error.instance.not.running");
            }
        }

        DevopsEnvCommandDTO devopsEnvCommandDTO = devopsEnvCommandService
                .baseQueryByObject(ObjectType.INSTANCE.getType(), instanceId);
        devopsEnvCommandDTO.setCommandType(type);
        devopsEnvCommandDTO.setStatus(CommandStatus.OPERATING.getStatus());
        devopsEnvCommandDTO.setId(null);
        devopsEnvCommandDTO = devopsEnvCommandService.baseCreate(devopsEnvCommandDTO);
        updateInstanceStatus(instanceId, devopsEnvCommandDTO.getId(), InstanceStatus.OPERATING.getStatus());


        //发送重启或停止实例的command
        Map<String, String> stopMap = new HashMap<>();
        stopMap.put(RELEASE_NAME, appServiceInstanceDTO.getCode());
        String payload = gson.toJson(stopMap);
        String instanceCommandType;
        if (CommandType.RESTART.getType().equals(type)) {
            instanceCommandType = HelmType.HELM_RELEASE_START.toValue();
        } else {
            instanceCommandType = HelmType.HELM_RELEASE_STOP.toValue();
        }

        agentCommandService.startOrStopInstance(payload, appServiceInstanceDTO.getCode(), instanceCommandType,
                devopsEnvironmentDTO.getCode(), devopsEnvCommandDTO.getId(), devopsEnvironmentDTO.getId(), devopsEnvironmentDTO.getClusterId());
    }


    private void initInstanceOverView(AppServiceInstanceOverViewVO appServiceInstanceOverViewVO, AppServiceInstanceOverViewDTO appServiceInstanceOverViewDTO,
                                      Long latestVersionId) {
        EnvVersionVO envVersionVO = new EnvVersionVO(
                appServiceInstanceOverViewDTO.getVersionId(),
                appServiceInstanceOverViewDTO.getVersion(),
                appServiceInstanceOverViewDTO.getInstanceId(),
                appServiceInstanceOverViewDTO.getInstanceCode(),
                appServiceInstanceOverViewDTO.getInstanceStatus());
        EnvInstanceVO envInstanceVO = new EnvInstanceVO(appServiceInstanceOverViewDTO.getEnvId());
        if (appServiceInstanceOverViewDTO.getVersionId().equals(latestVersionId)) {
            envVersionVO.setLatest(true);
        }
        envInstanceVO.addEnvVersionDTOS(envVersionVO);
        appServiceInstanceOverViewVO.appendEnvInstanceVOS(envInstanceVO);
        if (appServiceInstanceOverViewVO.getLatestVersionId().equals(appServiceInstanceOverViewDTO.getVersionId())) {
            appServiceInstanceOverViewVO.appendInstances(new EnvInstancesVO(
                    appServiceInstanceOverViewDTO.getInstanceId(), appServiceInstanceOverViewDTO.getInstanceCode(), appServiceInstanceOverViewDTO.getInstanceStatus()));
        }
    }


    private void initInstanceOverViewIfNotExist(AppServiceInstanceOverViewVO appServiceInstanceOverViewVO,
                                                AppServiceInstanceOverViewDTO appServiceInstanceOverViewDTO) {
        EnvInstanceVO envInstanceVO = appServiceInstanceOverViewVO.queryLastEnvInstanceVO();
        if (appServiceInstanceOverViewVO.getLatestVersionId().equals(appServiceInstanceOverViewDTO.getVersionId())) {
            appServiceInstanceOverViewVO.appendInstances(new EnvInstancesVO(
                    appServiceInstanceOverViewDTO.getInstanceId(), appServiceInstanceOverViewDTO.getInstanceCode(), appServiceInstanceOverViewDTO.getInstanceStatus()));
        }
        if (envInstanceVO.getEnvId().equals(appServiceInstanceOverViewDTO.getEnvId())) {
            EnvVersionVO envVersionVO = envInstanceVO.queryLastEnvVersionVO();
            if (envVersionVO.getVersion().equals(appServiceInstanceOverViewDTO.getVersion())) {
                envVersionVO.appendInstanceList(
                        appServiceInstanceOverViewDTO.getInstanceId(),
                        appServiceInstanceOverViewDTO.getInstanceCode(),
                        appServiceInstanceOverViewDTO.getInstanceStatus());
            } else {
                envInstanceVO.addEnvVersionDTOS(new EnvVersionVO(
                        appServiceInstanceOverViewDTO.getVersionId(),
                        appServiceInstanceOverViewDTO.getVersion(),
                        appServiceInstanceOverViewDTO.getInstanceId(),
                        appServiceInstanceOverViewDTO.getInstanceCode(),
                        appServiceInstanceOverViewDTO.getInstanceStatus()));
            }
        } else {
            EnvVersionVO envVersionVO = new EnvVersionVO(
                    appServiceInstanceOverViewDTO.getVersionId(),
                    appServiceInstanceOverViewDTO.getVersion(),
                    appServiceInstanceOverViewDTO.getInstanceId(),
                    appServiceInstanceOverViewDTO.getInstanceCode(),
                    appServiceInstanceOverViewDTO.getInstanceStatus());
            envInstanceVO = new EnvInstanceVO(appServiceInstanceOverViewDTO.getEnvId());
            if (appServiceInstanceOverViewDTO.getVersionId().equals(appServiceInstanceOverViewVO.getLatestVersionId())) {
                envVersionVO.setLatest(true);
            }
            envInstanceVO.addEnvVersionDTOS(envVersionVO);
            appServiceInstanceOverViewVO.appendEnvInstanceVOS(envInstanceVO);
        }
    }


    private AppServiceDTO createApplication(AppServiceRemoteDeployVO appServiceRemoteDeployVO) {
        String code = appServiceRemoteDeployVO.getAppServiceRemoteVO().getCode();
        String name = appServiceRemoteDeployVO.getAppServiceRemoteVO().getName();
        AppServiceDTO applicationDTO = applicationService.baseQueryByCodeWithNullProject(code);
        if (applicationDTO == null) {
            applicationDTO = new AppServiceDTO();
            DevopsConfigDTO harborConfigDTO = createConfig(HARBOR, appServiceRemoteDeployVO.getAppServiceRemoteVO().getCode(), appServiceRemoteDeployVO.getHarbor());
            DevopsConfigDTO chartConfigDTO = createConfig(CHART, appServiceRemoteDeployVO.getAppServiceRemoteVO().getCode(), appServiceRemoteDeployVO.getChart());
            applicationDTO.setType(appServiceRemoteDeployVO.getAppServiceRemoteVO().getType());
            applicationDTO.setCode(code);
            applicationDTO.setName(name);
            applicationDTO.setActive(true);
            applicationDTO.setSynchro(true);
            applicationDTO.setSkipCheckPermission(true);
            applicationDTO.setHarborConfigId(harborConfigDTO.getId());
            applicationDTO.setChartConfigId(chartConfigDTO.getId());
            return applicationService.baseCreate(applicationDTO);
        }
        return applicationDTO;
    }


    private AppServiceVersionDTO createVersion(AppServiceDTO applicationDTO, AppServiceVersionRemoteVO versionRemoteVO) {
        AppServiceVersionDTO versionDTO = appServiceVersionService.baseQueryByAppIdAndVersion(applicationDTO.getId(), versionRemoteVO.getVersion());
        if (versionDTO == null) {
            AppServiceVersionValueDTO versionValueDTO = new AppServiceVersionValueDTO();
            versionValueDTO.setValue(versionRemoteVO.getValues());
            versionValueDTO = appServiceVersionValueService.baseCreate(versionValueDTO);
            AppServiceVersionReadmeDTO versionReadmeDTO = new AppServiceVersionReadmeDTO();
            versionReadmeDTO.setReadme(versionRemoteVO.getReadMeValue());
            versionReadmeDTO = appServiceVersionReadmeService.baseCreate(versionReadmeDTO);
            versionDTO = new AppServiceVersionDTO();
            versionDTO.setAppServiceId(applicationDTO.getId());
            versionDTO.setValueId(versionValueDTO.getId());
            versionDTO.setReadmeValueId(versionReadmeDTO.getId());
            return appServiceVersionService.baseCreate(versionDTO);
        }
        return versionDTO;
    }

    /**
     * 创建远程配置
     *
     * @param type
     * @param code
     * @param configVO
     * @return
     */
    private DevopsConfigDTO createConfig(String type, String code, ConfigVO configVO) {
        String name = code + "-" + type;
        DevopsConfigDTO devopsConfigDTO = devopsConfigService.baseCheckByName(name);
        if (devopsConfigDTO == null) {
            devopsConfigDTO = new DevopsConfigDTO();
            devopsConfigDTO.setConfig(gson.toJson(configVO));
            devopsConfigDTO.setName(name);
            devopsConfigDTO.setType(type);
            return devopsConfigService.baseCreate(devopsConfigDTO);
        }
        return devopsConfigDTO;
    }


    private void updateInstanceStatus(Long instanceId, Long commandId, String status) {
        AppServiceInstanceDTO instanceDTO = baseQuery(instanceId);
        instanceDTO.setStatus(status);
        instanceDTO.setCommandId(commandId);
        baseUpdate(instanceDTO);
    }


    private List<ErrorLineVO> getErrorLine(String value) {
        List<ErrorLineVO> errorLines = new ArrayList<>();
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
            ErrorLineVO errorLineVO = new ErrorLineVO();
            errorLineVO.setLineNumber(lineNumbers.get(i));
            errorLineVO.setErrorMsg(errorMsg[i]);
            errorLines.add(errorLineVO);
        }
        return errorLines;
    }

    private C7nHelmRelease getC7NHelmRelease(String code, String repository, String appServiceCode,
                                             String version, String deployValue,
                                             Long deployVersionId, String secretName) {
        C7nHelmRelease c7nHelmRelease = new C7nHelmRelease();
        c7nHelmRelease.getMetadata().setName(code);
        c7nHelmRelease.getSpec().setRepoUrl(repository);
        c7nHelmRelease.getSpec().setChartName(appServiceCode);
        c7nHelmRelease.getSpec().setChartVersion(version);
        if (secretName != null) {
            c7nHelmRelease.getSpec().setImagePullSecrets(Arrays.asList(new ImagePullSecret(secretName)));
        }
        c7nHelmRelease.getSpec().setValues(
                getReplaceResult(appServiceVersionService.baseQueryValue(deployVersionId),
                        deployValue).getDeltaYaml().trim());
        return c7nHelmRelease;
    }


    private String getDeployTime(Long diff) {
        float num = (float) diff / (60 * 1000);
        DecimalFormat df = new DecimalFormat("0.00");
        return df.format(num);
    }


    private AppServiceInstanceDTO initApplicationInstanceDTO(AppServiceDeployVO appServiceDeployVO) {


        AppServiceInstanceDTO appServiceInstanceDTO = new AppServiceInstanceDTO();
        appServiceInstanceDTO.setAppServiceId(appServiceDeployVO.getAppServiceId());
        appServiceInstanceDTO.setEnvId(appServiceDeployVO.getEnvironmentId());
        appServiceInstanceDTO.setStatus(InstanceStatus.OPERATING.getStatus());
        appServiceDeployVO.setValueId(appServiceDeployVO.getValueId());
        if (appServiceDeployVO.getType().equals(UPDATE)) {
            AppServiceInstanceDTO oldAppServiceInstanceDTO = baseQuery(
                    appServiceDeployVO.getInstanceId());
            appServiceInstanceDTO.setCode(oldAppServiceInstanceDTO.getCode());
            appServiceInstanceDTO.setId(appServiceDeployVO.getInstanceId());
        }
        return appServiceInstanceDTO;
    }

    private DevopsEnvCommandDTO initDevopsEnvCommandDTO(AppServiceDeployVO appServiceDeployVO) {
        DevopsEnvCommandDTO devopsEnvCommandDTO = new DevopsEnvCommandDTO();
        switch (appServiceDeployVO.getType()) {
            case CREATE:
                devopsEnvCommandDTO.setCommandType(CommandType.CREATE.getType());
                break;
            case UPDATE:
                devopsEnvCommandDTO.setCommandType(CommandType.UPDATE.getType());
                break;
            default:
                devopsEnvCommandDTO.setCommandType(CommandType.DELETE.getType());
                break;
        }
        devopsEnvCommandDTO.setObjectVersionId(appServiceDeployVO.getAppServiceVersionId());
        devopsEnvCommandDTO.setObject(ObjectType.INSTANCE.getType());
        devopsEnvCommandDTO.setStatus(CommandStatus.OPERATING.getStatus());
        return devopsEnvCommandDTO;
    }

    private DevopsEnvCommandValueDTO initDevopsEnvCommandValueDTO
            (AppServiceDeployVO appServiceDeployVO) {
        DevopsEnvCommandValueDTO devopsEnvCommandValueDTO = new DevopsEnvCommandValueDTO();
        devopsEnvCommandValueDTO.setValue(appServiceDeployVO.getValues());
        return devopsEnvCommandValueDTO;
    }


    private AppServiceInstanceDTO restartDeploy(DevopsEnvironmentDTO
                                                         devopsEnvironmentDTO, AppServiceDTO
                                                         applicationDTO, AppServiceVersionDTO
                                                        appServiceVersionDTO, AppServiceInstanceDTO
                                                        appServiceInstanceDTO, DevopsEnvCommandValueDTO
                                                         devopsEnvCommandValueDTO, String secretCode) {
        DevopsEnvCommandDTO devopsEnvCommandDTO;
        baseUpdate(appServiceInstanceDTO);
        appServiceInstanceDTO = baseQuery(appServiceInstanceDTO.getId());
        devopsEnvCommandDTO = devopsEnvCommandService.baseQuery(appServiceInstanceDTO.getCommandId());
        devopsEnvCommandDTO.setId(null);
        devopsEnvCommandDTO.setCommandType(CommandType.UPDATE.getType());
        devopsEnvCommandDTO.setStatus(CommandStatus.OPERATING.getStatus());
        agentCommandService.deploy(applicationDTO, appServiceVersionDTO, appServiceInstanceDTO.getCode(), devopsEnvironmentDTO,
                devopsEnvCommandValueDTO.getValue(), devopsEnvCommandService.baseCreate(devopsEnvCommandDTO).getId(), secretCode);
        return appServiceInstanceDTO;
    }

    private String getSecret(AppServiceDTO appServiceDTO, String secretCode, DevopsEnvironmentDTO devopsEnvironmentDTO) {
        //如果应用绑定了私有镜像库,则处理secret
        DevopsConfigDTO devopsConfigDTO = devopsConfigService.queryRealConfig(appServiceDTO.getId(), APP_SERVICE,HARBOR);
        if (devopsConfigDTO != null) {
            ConfigVO configVO = gson.fromJson(devopsConfigDTO.getConfig(), ConfigVO.class);
            if (configVO.getPrivate() != null) {
                DevopsRegistrySecretDTO devopsRegistrySecretDTO = devopsRegistrySecretService.baseQueryByEnvAndId(devopsEnvironmentDTO.getCode(), devopsConfigDTO.getId());
                if (devopsRegistrySecretDTO == null) {
                    //当配置在当前环境下没有创建过secret.则新增secret信息，并通知k8s创建secret
                    List<DevopsRegistrySecretDTO> devopsRegistrySecretDTOS = devopsRegistrySecretService.baseListByConfig(devopsConfigDTO.getId());
                    if (devopsRegistrySecretDTOS.isEmpty()) {
                        secretCode = String.format("%s%s%s%s", "registry-secret-", devopsConfigDTO.getId(), "-", GenerateUUID.generateUUID().substring(0, 5));
                    } else {
                        secretCode = devopsRegistrySecretDTOS.get(0).getSecretCode();
                    }
                    devopsRegistrySecretDTO = new DevopsRegistrySecretDTO(devopsEnvironmentDTO.getId(), devopsConfigDTO.getId(), devopsEnvironmentDTO.getCode(), secretCode, devopsConfigDTO.getConfig());
                    devopsRegistrySecretService.baseCreate(devopsRegistrySecretDTO);
                    agentCommandService.operateSecret(devopsEnvironmentDTO.getClusterId(), devopsEnvironmentDTO.getCode(), secretCode, configVO, CREATE);
                } else {
                    //判断如果某个配置有发生过修改，则需要修改secret信息，并通知k8s更新secret
                    if (!devopsRegistrySecretDTO.getSecretDetail().equals(gson.toJson(devopsConfigDTO.getConfig()))) {
                        devopsRegistrySecretDTO.setSecretDetail(gson.toJson(devopsConfigDTO.getConfig()));
                        devopsRegistrySecretService.baseUpdate(devopsRegistrySecretDTO);
                        agentCommandService.operateSecret(devopsEnvironmentDTO.getClusterId(), devopsEnvironmentDTO.getCode(), devopsRegistrySecretDTO.getSecretCode(), configVO, UPDATE);
                    } else {
                        if (!devopsRegistrySecretDTO.getStatus()) {
                            agentCommandService.operateSecret(devopsEnvironmentDTO.getClusterId(), devopsEnvironmentDTO.getCode(), devopsRegistrySecretDTO.getSecretCode(), configVO, UPDATE);
                        }
                    }
                    secretCode = devopsRegistrySecretDTO.getSecretCode();
                }
            }
        }
        return secretCode;
    }


    /**
     * filter the pods that are associated with the daemonSet.
     *
     * @param devopsEnvPodDTOS the pods to be filtered
     * @param daemonSetName    the name of daemonSet
     * @return the pods
     */
    private List<DevopsEnvPodVO> filterPodsAssociatedWithDaemonSet(List<DevopsEnvPodVO> devopsEnvPodDTOS, String daemonSetName) {
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
    private List<DevopsEnvPodVO> filterPodsAssociatedWithStatefulSet(List<DevopsEnvPodVO> devopsEnvPodDTOS, String statefulSetName) {
        // statefulSet名称逻辑和daemonSet一致
        return filterPodsAssociatedWithDaemonSet(devopsEnvPodDTOS, statefulSetName);
    }


    private PageInfo<DeployDetailTableVO> getDeployDetailDTOS(PageInfo<DeployDTO> deployDTOPageInfo) {

        PageInfo<DeployDetailTableVO> pageDeployDetailDTOS = ConvertUtils.convertPage(deployDTOPageInfo, DeployDetailTableVO.class);

        List<DeployDetailTableVO> deployDetailTableVOS = new ArrayList<>();

        deployDTOPageInfo.getList().forEach(deployDTO -> {
            DeployDetailTableVO deployDetailTableVO = ConvertUtils.convertObject(deployDTO, DeployDetailTableVO.class);
            deployDetailTableVO.setDeployTime(
                    getDeployTime(deployDTO.getLastUpdateDate().getTime() - deployDTO.getCreationDate().getTime()));
            if (deployDTO.getCreatedBy() != 0) {
                IamUserDTO iamUserDTO = baseServiceClientOperator.queryUserByUserId(deployDTO.getCreatedBy());
                deployDetailTableVO.setLastUpdatedName(iamUserDTO.getRealName());
            }
            deployDetailTableVOS.add(deployDetailTableVO);
        });
        pageDeployDetailDTOS.setList(deployDetailTableVOS);
        return pageDeployDetailDTOS;
    }

    private String getAndCheckResourceDetail(Long instanceId, String resourceName, ResourceType resourceType) {
        String message = baseGetInstanceResourceDetailJson(instanceId, resourceName, resourceType);
        if (StringUtils.isEmpty(message)) {
            throw new CommonException("error.instance.resource.not.found", instanceId, resourceType.getType());
        }
        return message;
    }

    /**
     * filter the pods that are associated with the deployment.
     *
     * @param devopsEnvPodDTOS the pods to be filtered
     * @param deploymentName   the name of deployment
     * @return the pods
     */
    private List<DevopsEnvPodVO> filterPodsAssociated(List<DevopsEnvPodVO> devopsEnvPodDTOS, String deploymentName) {
        return devopsEnvPodDTOS.stream().filter(devopsEnvPodDTO -> {
                    String podName = devopsEnvPodDTO.getName();
                    String controllerNameFromPod = podName.substring(0,
                            podName.lastIndexOf('-', podName.lastIndexOf('-') - 1));
                    return deploymentName.equals(controllerNameFromPod);
                }
        ).collect(Collectors.toList());
    }
}
