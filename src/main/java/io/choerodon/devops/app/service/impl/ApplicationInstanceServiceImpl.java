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
import io.choerodon.devops.infra.feign.operator.IamServiceClientOperator;
import io.choerodon.devops.infra.gitops.ResourceConvertToYamlHandler;
import io.choerodon.devops.infra.gitops.ResourceFileCheckHandler;
import io.choerodon.devops.infra.handler.ClusterConnectionHandler;
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
    public static final String HARBOR = "harbor";
    public static final String CHART = "chart";


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
    private AgentCommandService agentCommandService;
    @Autowired
    private CommandSender commandSender;
    @Autowired
    private ClusterConnectionHandler clusterConnectionHandler;
    @Autowired
    private ApplicationInstanceMapper applicationInstanceMapper;
    @Autowired
    private DevopsEnvResourceService devopsEnvResourceService;
    @Autowired
    private DevopsEnvironmentService devopsEnvironmentService;
    @Autowired
    private DevopsEnvUserPermissionService devopsEnvUserPermissionService;
    @Autowired
    private IamServiceClientOperator iamServiceClientOperator;
    @Autowired
    private ApplicationVersionService applicationVersionService;
    @Autowired
    private DevopsDeployValueService devopsDeployValueService;
    @Autowired
    private TransactionalProducer producer;
    @Autowired
    private UserAttrService userAttrService;
    @Autowired
    private ApplicationService applicationService;
    @Autowired
    private DevopsProjectConfigService devopsProjectConfigService;
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
    private ApplicationVersionValueService applicationVersionValueService;
    @Autowired
    private ApplicationVersionReadmeService applicationVersionReadmeService;
    @Autowired
    private ResourceFileCheckHandler resourceFileCheckHandler;
    @Autowired
    private DevopsEnvApplicationMapper devopsEnvApplicationMapper;

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

        PageInfo<DevopsEnvPreviewInstanceVO> devopsEnvPreviewInstanceDTOPageInfo = new PageInfo<>();

        Map maps = gson.fromJson(params, Map.class);
        Map<String, Object> searchParamMap = TypeUtil.cast(maps.get(TypeUtil.SEARCH_PARAM));
        String paramMap = TypeUtil.cast(maps.get(TypeUtil.PARAM));
        PageInfo<ApplicationInstanceDTO> applicationInstanceDTOPageInfo = PageHelper.startPage(pageRequest.getPage(), pageRequest.getSize(), PageRequestUtil.getOrderBy(pageRequest)).doSelectPageInfo(() ->
                applicationInstanceMapper
                        .listApplicationInstance(projectId, envId, versionId, appId, instanceId, searchParamMap, paramMap));

        BeanUtils.copyProperties(applicationInstanceDTOPageInfo, devopsEnvPreviewInstanceDTOPageInfo);

        return devopsEnvPreviewInstanceDTOPageInfo;

    }


    @Override
    public List<ApplicationInstanceOverViewVO> listApplicationInstanceOverView(Long projectId, Long appId) {


        //查询出当前用户有权限的环境列表，如果是项目所有者，则有全部环境权限
        List<Long> permissionEnvIds = devopsEnvUserPermissionService
                .listByUserId(TypeUtil.objToLong(GitUserNameUtil.getUserId())).stream()
                .filter(DevopsEnvUserPermissionDTO::getPermitted).map(DevopsEnvUserPermissionDTO::getEnvId)
                .collect(Collectors.toList());

        ProjectDTO projectDTO = iamServiceClientOperator.queryIamProjectById(projectId);
        if (iamServiceClientOperator.isProjectOwner(TypeUtil.objToLong(GitUserNameUtil.getUserId()), projectDTO)) {
            permissionEnvIds = devopsEnvironmentService.baseListByProjectId(projectId).stream()
                    .map(DevopsEnvironmentDTO::getId).collect(Collectors.toList());
        }

        List<ApplicationInstanceOverViewDTO> applicationInstanceOverViewDTOS = baseListApplicationInstanceOverView(projectId, appId,
                permissionEnvIds);

        List<ApplicationLatestVersionDTO> appLatestVersionList = applicationVersionService.baseListAppNewestVersion(projectId);

        Map<Long, ApplicationLatestVersionDTO> latestVersionList = appLatestVersionList.stream()
                .collect(Collectors.toMap(ApplicationLatestVersionDTO::getAppId, t -> t, (a, b) -> b));

        //查询部署总览，每个应用最新的版本以及在每个环境每个实例部署的版本
        Map<Long, Integer> appInstancesListMap = new HashMap<>();
        List<ApplicationInstanceOverViewVO> applicationInstanceOverViewVOS = new ArrayList<>();
        applicationInstanceOverViewDTOS.forEach(t -> {
            ApplicationInstanceOverViewVO applicationInstanceOverViewVO = new ApplicationInstanceOverViewVO();
            if (appInstancesListMap.get(t.getAppId()) == null) {
                if (t.getInstanceId() != null
                        || t.getVersionId().equals(latestVersionList.get(t.getAppId()).getVersionId())) {
                    applicationInstanceOverViewVO = new ApplicationInstanceOverViewVO(
                            t.getAppId(),
                            t.getPublishLevel(),
                            t.getAppName(),
                            t.getAppCode(),
                            latestVersionList.get(t.getAppId()).getVersionId(),
                            latestVersionList.get(t.getAppId()).getVersion());
                    applicationInstanceOverViewVO.setProjectId(t.getProjectId());
                    if (t.getInstanceId() != null) {
                        initInstanceOverView(applicationInstanceOverViewVO, t, latestVersionList.get(t.getAppId()).getVersionId());
                    }
                    appInstancesListMap.put(t.getAppId(), applicationInstanceOverViewVOS.size());
                    applicationInstanceOverViewVOS.add(applicationInstanceOverViewVO);
                }
            } else {
                applicationInstanceOverViewVO = applicationInstanceOverViewVOS.get(appInstancesListMap.get(t.getAppId()));
                initInstanceOverViewIfNotExist(applicationInstanceOverViewVO, t);
            }
            if (t.getInstanceId() != null
                    && t.getVersion().equalsIgnoreCase(applicationInstanceOverViewVO.getLatestVersion())) {
                applicationInstanceOverViewVO.addLatestVersionRunning();
            }
        });
        return applicationInstanceOverViewVOS;
    }


    @Override
    public InstanceValueVO queryDeployValue(String type, Long instanceId, Long versionId) {
        InstanceValueVO instanceValueVO = new InstanceValueVO();
        String versionValue = FileUtil.checkValueFormat(applicationVersionService.baseQueryValue(versionId));

        if (type.equals(UPDATE)) {
            ApplicationInstanceDTO applicationInstanceDTO = baseQuery(instanceId);
            if (applicationInstanceDTO.getValueId() != null) {
                DevopsDeployValueDTO devopsDeployValueDTO = devopsDeployValueService.baseQueryById(applicationInstanceDTO.getValueId());
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
    public InstanceValueVO queryUpgradeValue(Long instanceId, Long versionId) {
        ApplicationInstanceDTO applicationInstanceDTO = baseQuery(instanceId);
        String yaml = FileUtil.checkValueFormat(baseQueryValueByInstanceId(instanceId));
        String versionValue = applicationVersionService.baseQueryValue(versionId);
        InstanceValueVO instanceValueVO = new InstanceValueVO();
        if (applicationInstanceDTO.getValueId() != null) {
            DevopsDeployValueDTO devopsDeployValueDTO = devopsDeployValueService.baseQueryById(applicationInstanceDTO.getValueId());
            instanceValueVO.setName(devopsDeployValueDTO.getName());
            instanceValueVO.setId(devopsDeployValueDTO.getId());
            instanceValueVO.setObjectVersionNumber(devopsDeployValueDTO.getObjectVersionNumber());
        }
        instanceValueVO.setYaml(getReplaceResult(versionValue, yaml).getYaml());
        return instanceValueVO;
    }

    @Override
    public DeployTimeVO listDeployTime(Long projectId, Long envId, Long[] appIds,
                                       Date startTime, Date endTime) {

        DeployTimeVO deployTimeVO = new DeployTimeVO();

        if (appIds.length == 0) {
            return deployTimeVO;
        }

        List<DeployDTO> deployDTOS = baseListDeployTime(projectId, envId, appIds, startTime, endTime);
        List<Date> creationDates = deployDTOS.stream().map(DeployDTO::getCreationDate).collect(Collectors.toList());

        //操作时间排序
        creationDates = new ArrayList<>(new HashSet<>(creationDates)).stream().sorted(Comparator.naturalOrder())
                .collect(Collectors.toList());

        List<DeployAppVO> deployAppVOS = new ArrayList<>();

        //以应用为维度分组
        Map<String, List<DeployDTO>> resultMaps = deployDTOS.stream()
                .collect(Collectors.groupingBy(DeployDTO::getAppName));

        resultMaps.forEach((key, value) -> {
            DeployAppVO deployAppVO = new DeployAppVO();
            List<DeployDetailVO> deployDetailVOS = new ArrayList<>();
            deployAppVO.setAppName(key);
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
                                                 Long appId, Date startTime, Date endTime) {
        if (envIds.length == 0) {
            return new DeployFrequencyVO();
        }
        List<DeployDTO> deployDTOS = baselistDeployFrequency(projectId, envIds, appId, startTime, endTime);

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
                                                                  Long appId, Date startTime, Date endTime) {
        if (envIds.length == 0) {
            return new PageInfo<>();
        }
        PageInfo<DeployDTO> deployDTOPageInfo = basePageDeployFrequencyTable(projectId, pageRequest,
                envIds, appId, startTime, endTime);
        return getDeployDetailDTOS(deployDTOPageInfo);
    }


    @Override
    public PageInfo<DeployDetailTableVO> pageDeployTimeTable(Long projectId, PageRequest pageRequest,
                                                             Long[] appIds, Long envId,
                                                             Date startTime, Date endTime) {
        if (appIds.length == 0) {
            return new PageInfo<>();
        }
        PageInfo<DeployDTO> deployDTOS = basePageDeployTimeTable(projectId, pageRequest, envId,
                appIds, startTime, endTime);
        return getDeployDetailDTOS(deployDTOS);
    }


    @Override
    public void deployTestApp(ApplicationDeployVO applicationDeployVO) {

        String versionValue = applicationVersionService.baseQueryValue(applicationDeployVO.getAppVersionId());
        ApplicationDTO applicationDTO = applicationService.baseQuery(applicationDeployVO.getAppId());

        DevopsEnvironmentDTO devopsEnvironmentDTO = new DevopsEnvironmentDTO();
        devopsEnvironmentDTO.setCode(CHOERODON);
        devopsEnvironmentDTO.setClusterId(applicationDeployVO.getEnvironmentId());
        String secretCode = getSecret(applicationDTO, null, devopsEnvironmentDTO);

        ApplicationVersionDTO applicationVersionDTO = applicationVersionService.baseQuery(applicationDeployVO.getAppVersionId());
        FileUtil.checkYamlFormat(applicationDeployVO.getValues());
        String deployValue = getReplaceResult(versionValue,
                applicationDeployVO.getValues()).getDeltaYaml().trim();
        agentCommandService.deployTestApp(applicationDTO, applicationVersionDTO, applicationDeployVO.getInstanceName(), secretCode, applicationDeployVO.getEnvironmentId(), deployValue);
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
        String paramMap = TypeUtil.cast(maps.get(TypeUtil.PARAM));

        List<ApplicationInstanceDTO> applicationInstanceDTOS = applicationInstanceMapper
                .listApplicationInstance(projectId, envId, null, null, null, searchParamMap, paramMap);

        List<ApplicationInstanceVO> applicationInstanceVOS =
                ConvertUtils.convertList(applicationInstanceDTOS, ApplicationInstanceVO.class);

        //以app为维度给实例分组
        Map<Long, List<ApplicationInstanceVO>> resultMaps = applicationInstanceVOS.stream()
                .collect(Collectors.groupingBy(t -> t.getAppId()));
        DevopsEnvPreviewVO devopsEnvPreviewVO = new DevopsEnvPreviewVO();
        List<DevopsEnvPreviewAppVO> devopsEnvPreviewAppVOS = new ArrayList<>();
        resultMaps.forEach((key, value) -> {
            DevopsEnvPreviewAppVO devopsEnvPreviewAppVO = new DevopsEnvPreviewAppVO();
            devopsEnvPreviewAppVO.setAppName(value.get(0).getAppName());
            devopsEnvPreviewAppVO.setAppCode(value.get(0).getAppCode());
            devopsEnvPreviewAppVO.setProjectId(value.get(0).getProjectId());

            //设置应用所属的实例
            devopsEnvPreviewAppVO.setApplicationInstanceVOS(applicationInstanceVOS);

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
    public ApplicationInstanceVO createOrUpdate(ApplicationDeployVO applicationDeployVO) {

        DevopsEnvironmentDTO devopsEnvironmentDTO = devopsEnvironmentService.baseQueryById(applicationDeployVO.getEnvironmentId());

        UserAttrDTO userAttrDTO = userAttrService.baseQueryById(TypeUtil.objToLong(GitUserNameUtil.getUserId()));

        //校验环境相关信息
        devopsEnvironmentService.checkEnv(devopsEnvironmentDTO, userAttrDTO);

        //校验values
        FileUtil.checkYamlFormat(applicationDeployVO.getValues());


        ApplicationDTO applicationDTO = applicationService.baseQuery(applicationDeployVO.getAppId());
        ApplicationVersionDTO applicationVersionDTO =
                applicationVersionService.baseQuery(applicationDeployVO.getAppVersionId());

        //初始化ApplicationInstanceDTO,DevopsEnvCommandDTO,DevopsEnvCommandValueDTO
        ApplicationInstanceDTO applicationInstanceDTO = initApplicationInstanceDTO(applicationDeployVO);
        DevopsEnvCommandDTO devopsEnvCommandDTO = initDevopsEnvCommandDTO(applicationDeployVO);
        DevopsEnvCommandValueDTO devopsEnvCommandValueDTO = initDevopsEnvCommandValueDTO(applicationDeployVO);

        String secretCode = null;
        //获取部署实例时授权secret的code
        secretCode = getSecret(applicationDTO, secretCode, devopsEnvironmentDTO);

        // 初始化自定义实例名
        String code;
        if (applicationDeployVO.getType().equals(CREATE)) {
            if (applicationDeployVO.getInstanceName() == null || applicationDeployVO.getInstanceName().trim().equals("")) {
                code = String.format("%s-%s", applicationDTO.getCode(), GenerateUUID.generateUUID().substring(0, 5));
            } else {
                code = applicationDeployVO.getInstanceName();
            }
        } else {
            code = applicationInstanceDTO.getCode();
            //更新实例的时候校验gitops库文件是否存在,处理部署实例时，由于没有创gitops文件导致的部署失败
            resourceFileCheckHandler.check(devopsEnvironmentDTO, applicationDeployVO.getAppInstanceId(), code, C7NHELM_RELEASE);

            //从未关联部署配置到关联部署配置，或者从一个部署配置关联另外一个部署配置，如果values是一样的，虽然getIsNotChange为false,但是此时也应该直接设置为isNotChange为true
            DevopsEnvCommandDTO oldDevopsEnvCommandE = devopsEnvCommandService.baseQuery(baseQuery(applicationInstanceDTO.getId()).getCommandId());
            String deployValue = baseQueryValueByInstanceId(applicationInstanceDTO.getId());
            if (applicationDeployVO.getAppVersionId().equals(oldDevopsEnvCommandE.getObjectVersionId()) && deployValue.equals(applicationDeployVO.getValues())) {
                applicationDeployVO.setIsNotChange(true);
            }
        }

        //更新时候，如果isNotChange的值为true，则直接向agent发送更新指令，不走gitops,否则走操作gitops库文件逻辑
        if (applicationDeployVO.getIsNotChange()) {
            applicationInstanceDTO = restartDeploy(devopsEnvironmentDTO, applicationDTO, applicationVersionDTO, applicationInstanceDTO, devopsEnvCommandValueDTO, secretCode);
        } else {
            //存储数据
            if (applicationDeployVO.getType().equals(CREATE)) {
                createEnvAppRelationShipIfNon(applicationDeployVO.getAppId(), applicationDeployVO.getEnvironmentId());
                applicationInstanceDTO.setCode(code);
                applicationInstanceDTO.setId(baseCreate(applicationInstanceDTO).getId());
                devopsEnvCommandDTO.setObjectId(applicationInstanceDTO.getId());
                devopsEnvCommandDTO.setValueId(devopsEnvCommandValueService.baseCreate(devopsEnvCommandValueDTO).getId());
                applicationInstanceDTO.setCommandId(devopsEnvCommandService.baseCreate(devopsEnvCommandDTO).getId());
                baseUpdate(applicationInstanceDTO);
            } else {
                devopsEnvCommandDTO.setObjectId(applicationInstanceDTO.getId());
                devopsEnvCommandDTO.setValueId(devopsEnvCommandValueService.baseCreate(devopsEnvCommandValueDTO).getId());
                applicationInstanceDTO.setCommandId(devopsEnvCommandService.baseCreate(devopsEnvCommandDTO).getId());
                baseUpdate(applicationInstanceDTO);
            }
        }
        applicationDeployVO.setAppInstanceId(applicationInstanceDTO.getId());
        applicationDeployVO.setInstanceName(code);
        InstanceSagaPayload instanceSagaPayload = new InstanceSagaPayload(applicationDTO.getProjectId(), userAttrDTO.getGitlabUserId(), secretCode);
        instanceSagaPayload.setApplicationDTO(applicationDTO);
        instanceSagaPayload.setApplicationVersionDTO(applicationVersionDTO);
        instanceSagaPayload.setApplicationDeployVO(applicationDeployVO);
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
        return ConvertUtils.convertObject(applicationInstanceDTO, ApplicationInstanceVO.class);
    }

    /**
     * 为环境和应用创建关联关系如果不存在
     *
     * @param appId 应用id
     * @param envId 环境id
     */
    private void createEnvAppRelationShipIfNon(Long appId, Long envId) {
        DevopsEnvApplicationDTO devopsEnvApplicationDTO = new DevopsEnvApplicationDTO();
        devopsEnvApplicationDTO.setAppId(appId);
        devopsEnvApplicationDTO.setEnvId(envId);
        devopsEnvApplicationMapper.insertIgnore(devopsEnvApplicationDTO);
    }

    @Override
    public void createInstanceBySaga(InstanceSagaPayload instanceSagaPayload) {

        try {
            //判断当前容器目录下是否存在环境对应的gitops文件目录，不存在则克隆
            String filePath = clusterConnectionHandler.handDevopsEnvGitRepository(instanceSagaPayload.getProjectId(), instanceSagaPayload.getDevopsEnvironmentDTO().getCode(), instanceSagaPayload.getDevopsEnvironmentDTO().getEnvIdRsa());

            //在gitops库处理instance文件
            ResourceConvertToYamlHandler<C7nHelmRelease> resourceConvertToYamlHandler = new ResourceConvertToYamlHandler<>();
            resourceConvertToYamlHandler.setType(getC7NHelmRelease(
                    instanceSagaPayload.getApplicationDeployVO().getInstanceName(), instanceSagaPayload.getApplicationVersionDTO().getRepository(), instanceSagaPayload.getApplicationDTO().getCode(), instanceSagaPayload.getApplicationVersionDTO().getVersion(), instanceSagaPayload.getApplicationDeployVO().getValues(), instanceSagaPayload.getApplicationDeployVO().getAppVersionId(), instanceSagaPayload.getSecretCode()));

            resourceConvertToYamlHandler.operationEnvGitlabFile(
                    RELEASE_PREFIX + instanceSagaPayload.getApplicationDeployVO().getInstanceName(),
                    instanceSagaPayload.getDevopsEnvironmentDTO().getGitlabEnvProjectId().intValue(),
                    instanceSagaPayload.getApplicationDeployVO().getType(),
                    instanceSagaPayload.getGitlabUserId(),
                    instanceSagaPayload.getApplicationDeployVO().getAppInstanceId(), C7NHELM_RELEASE, null, false, instanceSagaPayload.getDevopsEnvironmentDTO().getId(), filePath);
        } catch (Exception e) {
            //有异常更新实例以及command的状态
            ApplicationInstanceDTO applicationInstanceDTO = baseQuery(instanceSagaPayload.getApplicationDeployVO().getAppInstanceId());
            applicationInstanceDTO.setStatus(CommandStatus.FAILED.getStatus());
            baseUpdate(applicationInstanceDTO);
            DevopsEnvCommandDTO devopsEnvCommandDTO = devopsEnvCommandService.baseQuery(applicationInstanceDTO.getCommandId());
            devopsEnvCommandDTO.setStatus(CommandStatus.FAILED.getStatus());
            devopsEnvCommandDTO.setError("create or update gitOps file failed!");
            devopsEnvCommandService.baseUpdate(devopsEnvCommandDTO);
        }
    }


    @Override
    public ApplicationInstanceVO createOrUpdateByGitOps(ApplicationDeployVO applicationDeployVO, Long userId) {
        DevopsEnvironmentDTO devopsEnvironmentDTO = devopsEnvironmentService.baseQueryById(applicationDeployVO.getEnvironmentId());
        //校验环境是否连接
        clusterConnectionHandler.checkEnvConnection(devopsEnvironmentDTO.getClusterId());

        //校验values
        FileUtil.checkYamlFormat(applicationDeployVO.getValues());

        //初始化ApplicationInstanceE,DevopsEnvCommandE,DevopsEnvCommandValueE
        ApplicationInstanceDTO applicationInstanceDTO = initApplicationInstanceDTO(applicationDeployVO);
        DevopsEnvCommandDTO devopsEnvCommandDTO = initDevopsEnvCommandDTO(applicationDeployVO);
        DevopsEnvCommandValueDTO devopsEnvCommandValueDTO = initDevopsEnvCommandValueDTO(applicationDeployVO);

        //实例相关对象数据库操作
        if (applicationDeployVO.getType().equals(CREATE)) {
            applicationInstanceDTO.setCode(applicationDeployVO.getInstanceName());
            applicationInstanceDTO.setId(baseCreate(applicationInstanceDTO).getId());
        } else {
            baseUpdate(applicationInstanceDTO);
        }
        devopsEnvCommandDTO.setCreatedBy(userId);
        devopsEnvCommandDTO.setObjectId(applicationInstanceDTO.getId());
        devopsEnvCommandDTO.setValueId(devopsEnvCommandValueService.baseCreate(devopsEnvCommandValueDTO).getId());
        applicationInstanceDTO.setCommandId(devopsEnvCommandService.baseCreate(devopsEnvCommandDTO).getId());
        baseUpdate(applicationInstanceDTO);
        return ConvertUtils.convertObject(applicationInstanceDTO, ApplicationInstanceVO.class);
    }


    @Override
    public List<RunningInstanceVO> listRunningInstance(Long projectId, Long appId, Long appVersionId, Long envId) {
        return ConvertUtils.convertList(baseListByOptions(projectId, appId, appVersionId, envId), RunningInstanceVO.class);
    }

    @Override
    public List<RunningInstanceVO> listByAppIdAndEnvId(Long projectId, Long appId, Long envId) {
        return ConvertUtils.convertList(baseListByAppIdAndEnvId(projectId, appId, envId), RunningInstanceVO.class);
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
        ApplicationInstanceDTO applicationInstanceDTO = baseQuery(instanceId);

        DevopsEnvironmentDTO devopsEnvironmentDTO = devopsEnvironmentService.baseQueryById(applicationInstanceDTO.getEnvId());

        UserAttrDTO userAttrDTO = userAttrService.baseQueryById(TypeUtil.objToLong(GitUserNameUtil.getUserId()));

        //校验环境相关信息
        devopsEnvironmentService.checkEnv(devopsEnvironmentDTO, userAttrDTO);

        DevopsEnvCommandDTO devopsEnvCommandDTO = devopsEnvCommandService.baseQuery(applicationInstanceDTO.getCommandId());
        ApplicationDTO applicationDTO = applicationService.baseQuery(applicationInstanceDTO.getAppId());
        ApplicationVersionDTO applicationVersionDTO = applicationVersionService
                .baseQuery(devopsEnvCommandDTO.getObjectVersionId());

        String value = baseQueryValueByInstanceId(instanceId);

        devopsEnvCommandDTO.setId(null);
        devopsEnvCommandDTO.setCommandType(CommandType.UPDATE.getType());
        devopsEnvCommandDTO.setStatus(CommandStatus.OPERATING.getStatus());
        devopsEnvCommandDTO = devopsEnvCommandService.baseCreate(devopsEnvCommandDTO);

        updateInstanceStatus(instanceId, devopsEnvCommandDTO.getId(), InstanceStatus.OPERATIING.getStatus());

        //获取授权secret
        String secretCode = getSecret(applicationDTO, null, devopsEnvironmentDTO);

        //发送实例重新部署的command
        agentCommandService.deploy(applicationDTO, applicationVersionDTO, applicationInstanceDTO.getCode(), devopsEnvironmentDTO, value, devopsEnvCommandDTO.getId(), secretCode);
    }


    @Override
    @Transactional(rollbackFor = Exception.class)

    public void deleteInstance(Long instanceId) {

        ApplicationInstanceDTO applicationInstanceDTO = baseQuery(instanceId);

        DevopsEnvironmentDTO devopsEnvironmentDTO = devopsEnvironmentService.baseQueryById(applicationInstanceDTO.getEnvId());

        UserAttrDTO userAttrDTO = userAttrService.baseQueryById(TypeUtil.objToLong(GitUserNameUtil.getUserId()));

        //校验环境相关信息
        devopsEnvironmentService.checkEnv(devopsEnvironmentDTO, userAttrDTO);

        DevopsEnvCommandDTO devopsEnvCommandDTO = devopsEnvCommandService.baseQuery(applicationInstanceDTO.getCommandId());
        devopsEnvCommandDTO.setCommandType(CommandType.DELETE.getType());
        devopsEnvCommandDTO.setStatus(CommandStatus.OPERATING.getStatus());
        devopsEnvCommandDTO.setId(null);
        devopsEnvCommandDTO = devopsEnvCommandService.baseCreate(devopsEnvCommandDTO);

        updateInstanceStatus(instanceId, devopsEnvCommandDTO.getId(), InstanceStatus.OPERATIING.getStatus());

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
                    RELEASE_PREFIX + applicationInstanceDTO.getCode() + YAML_SUFFIX)) {
                gitlabServiceClientOperator.deleteFile(
                        TypeUtil.objToInteger(devopsEnvironmentDTO.getGitlabEnvProjectId()),
                        RELEASE_PREFIX + applicationInstanceDTO.getCode() + YAML_SUFFIX,
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
            metadata.setName(applicationInstanceDTO.getCode());
            c7nHelmRelease.setMetadata(metadata);
            resourceConvertToYamlHandler.setType(c7nHelmRelease);
            Integer projectId = TypeUtil.objToInteger(devopsEnvironmentDTO.getGitlabEnvProjectId());
            resourceConvertToYamlHandler.operationEnvGitlabFile(
                    RELEASE_PREFIX + applicationInstanceDTO.getCode(),
                    projectId,
                    "delete",
                    userAttrDTO.getGitlabUserId(),
                    applicationInstanceDTO.getId(), C7NHELM_RELEASE, null, false, devopsEnvironmentDTO.getId(), path);
        }
    }


    @Override
    public InstanceValueVO queryPreviewValues(InstanceValueVO previewInstanceValueVO, Long appVersionId) {
        String versionValue = applicationVersionService.baseQueryValue(appVersionId);
        try {
            FileUtil.checkYamlFormat(previewInstanceValueVO.getYaml());
        } catch (Exception e) {
            throw new CommonException(e.getMessage(), e);
        }
        return getReplaceResult(versionValue, previewInstanceValueVO.getYaml());
    }

    @Override
    public void instanceDeleteByGitOps(Long instanceId) {
        ApplicationInstanceDTO instanceDTO = baseQuery(instanceId);

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
        baseCheckName(code, envId);
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
    @Transactional
    public ApplicationInstanceVO deployRemoteApp(ApplicationRemoteDeployVO applicationRemoteDeployVO) {

        //创建远程应用
        ApplicationDTO applicationDTO = createApplication(applicationRemoteDeployVO);

        //创建远程应用版本
        ApplicationVersionDTO applicationVersionDTO = createVersion(applicationDTO, applicationRemoteDeployVO.getApplicationVersionRemoteVO());

        //初始化部署所需数据
        ApplicationDeployVO applicationDeployVO = ConvertUtils.convertObject(applicationRemoteDeployVO, ApplicationDeployVO.class);
        applicationDeployVO.setAppId(applicationDTO.getId());
        applicationDeployVO.setAppVersionId(applicationVersionDTO.getId());
        applicationDeployVO.setValues(applicationRemoteDeployVO.getApplicationVersionRemoteVO().getValues());
        return createOrUpdate(applicationDeployVO);
    }


    @Override
    public ApplicationInstanceDTO baseQueryByCodeAndEnv(String code, Long envId) {
        ApplicationInstanceDTO applicationInstanceDTO = new ApplicationInstanceDTO();
        applicationInstanceDTO.setCode(code);
        applicationInstanceDTO.setEnvId(envId);
        return applicationInstanceMapper.selectOne(applicationInstanceDTO);
    }

    public ApplicationInstanceDTO baseCreate(ApplicationInstanceDTO applicationInstanceDTO) {
        if (applicationInstanceMapper.insert(applicationInstanceDTO) != 1) {
            throw new CommonException("error.application.instance.create");
        }
        return applicationInstanceDTO;
    }

    public ApplicationInstanceDTO baseQuery(Long id) {
        return applicationInstanceMapper.selectByPrimaryKey(id);
    }

    public List<ApplicationInstanceDTO> baseListByOptions(Long projectId, Long appId, Long appVersionId, Long envId) {
        return applicationInstanceMapper.listApplicationInstanceCode(
                projectId, envId, appVersionId, appId);
    }

    public List<ApplicationInstanceDTO> baseListByAppIdAndEnvId(Long projectId, Long appId, Long envId) {
        return applicationInstanceMapper.listRunningAndFailedInstance(
                projectId, envId, appId);
    }

    public int baseCountByOptions(Long envId, Long appId, String appInstanceCode) {
        return applicationInstanceMapper.countByOptions(envId, appId, appInstanceCode);
    }

    public String baseQueryValueByEnvIdAndAppId(Long
                                                        envId, Long appId) {
        return applicationInstanceMapper.queryValueByEnvIdAndAppId(envId, appId);
    }

    public void baseUpdate(ApplicationInstanceDTO applicationInstanceDTO) {
        applicationInstanceDTO.setObjectVersionNumber(
                applicationInstanceMapper.selectByPrimaryKey(applicationInstanceDTO.getId()).getObjectVersionNumber());
        if (applicationInstanceMapper.updateByPrimaryKeySelective(applicationInstanceDTO) != 1) {
            throw new CommonException("error.instance.update");
        }
    }


    @Override
    public List<ApplicationInstanceDTO> baseListByEnvId(Long envId) {
        ApplicationInstanceDTO applicationInstanceDTO = new ApplicationInstanceDTO();
        applicationInstanceDTO.setEnvId(envId);
        return applicationInstanceMapper
                .select(applicationInstanceDTO);
    }

    public List<ApplicationInstanceOverViewDTO> baseListApplicationInstanceOverView(Long projectId, Long appId, List<Long> envIds) {
        if (envIds != null && envIds.isEmpty()) {
            envIds = null;
        }
        return applicationInstanceMapper.listApplicationInstanceOverView(projectId, appId, envIds);
    }

    @Override
    public List<ApplicationInstanceDTO> baseList() {
        return applicationInstanceMapper.selectAll();
    }

    @Override
    public String baseQueryValueByInstanceId(Long instanceId) {
        return applicationInstanceMapper.queryByInstanceId(instanceId);
    }

    public void baseDelete(Long id) {
        applicationInstanceMapper.deleteByPrimaryKey(id);
    }


    public List<DeployDTO> baseListDeployTime(Long projectId, Long envId, Long[] appIds, Date startTime, Date endTime) {
        return applicationInstanceMapper
                .listDeployTime(projectId, envId, appIds, new java.sql.Date(startTime.getTime()),
                        new java.sql.Date(endTime.getTime()));
    }

    public List<DeployDTO> baselistDeployFrequency(Long projectId, Long[] envIds, Long appId,
                                                   Date startTime, Date endTime) {
        return applicationInstanceMapper
                .listDeployFrequency(projectId, envIds, appId, new java.sql.Date(startTime.getTime()),
                        new java.sql.Date(endTime.getTime()));
    }

    public PageInfo<DeployDTO> basePageDeployFrequencyTable(Long projectId, PageRequest pageRequest, Long[] envIds, Long appId,
                                                            Date startTime, Date endTime) {
        return PageHelper.startPage(pageRequest.getPage(), pageRequest.getSize(), PageRequestUtil.getOrderBy(pageRequest)).doSelectPageInfo(() ->
                applicationInstanceMapper
                        .listDeployFrequency(projectId, envIds, appId, new java.sql.Date(startTime.getTime()),
                                new java.sql.Date(endTime.getTime())));
    }

    public PageInfo<DeployDTO> basePageDeployTimeTable(Long projectId, PageRequest pageRequest, Long envId, Long[] appIds,
                                                       Date startTime, Date endTime) {
        return PageHelper.startPage(pageRequest.getPage(), pageRequest.getSize(), PageRequestUtil.getOrderBy(pageRequest)).doSelectPageInfo(() ->
                applicationInstanceMapper
                        .listDeployTime(projectId, envId, appIds, new java.sql.Date(startTime.getTime()),
                                new java.sql.Date(endTime.getTime())));
    }

    @Override
    public List<ApplicationInstanceDTO> baseListByAppId(Long appId) {
        ApplicationInstanceDTO applicationInstanceDTO = new ApplicationInstanceDTO();
        applicationInstanceDTO.setAppId(appId);
        return applicationInstanceMapper.select(applicationInstanceDTO);
    }

    @Override
    public void deleteByEnvId(Long envId) {
        ApplicationInstanceDTO applicationInstanceDTO = new ApplicationInstanceDTO();
        applicationInstanceDTO.setEnvId(envId);
        applicationInstanceMapper.delete(applicationInstanceDTO);
    }

    @Override
    public List<ApplicationInstanceDTO> baseListByValueId(Long valueId) {
        ApplicationInstanceDTO applicationInstanceDTO = new ApplicationInstanceDTO();
        applicationInstanceDTO.setValueId(valueId);
        return applicationInstanceMapper.select(applicationInstanceDTO);
    }


    public void baseCheckName(String instanceName, Long envId) {
        ApplicationInstanceDTO applicationInstanceDTO = new ApplicationInstanceDTO();
        applicationInstanceDTO.setCode(instanceName);
        applicationInstanceDTO.setEnvId(envId);
        if (applicationInstanceMapper.selectOne(applicationInstanceDTO) != null) {
            throw new CommonException("error.app.instance.name.already.exist");
        }
    }

    public String baseGetInstanceResourceDetailJson(Long instanceId, String resourceName, ResourceType resourceType) {
        return applicationInstanceMapper.getInstanceResourceDetailJson(instanceId, resourceName, resourceType.getType());
    }

    public void baseDeleteInstanceRelInfo(Long instanceId) {
        applicationInstanceMapper.deleteInstanceRelInfo(instanceId);
    }


    private void handleStartOrStopInstance(Long instanceId, String type) {

        ApplicationInstanceDTO applicationInstanceDTO = baseQuery(instanceId);

        DevopsEnvironmentDTO devopsEnvironmentDTO = devopsEnvironmentService.baseQueryById(applicationInstanceDTO.getEnvId());

        UserAttrDTO userAttrDTO = userAttrService.baseQueryById(TypeUtil.objToLong(GitUserNameUtil.getUserId()));

        //校验环境相关信息
        devopsEnvironmentService.checkEnv(devopsEnvironmentDTO, userAttrDTO);

        if (CommandType.RESTART.getType().equals(type)) {
            if (!applicationInstanceDTO.getStatus().equals(InstanceStatus.STOPPED.getStatus())) {
                throw new CommonException("error.instance.not.stop");
            }
        } else {
            if (!applicationInstanceDTO.getStatus().equals(InstanceStatus.RUNNING.getStatus())) {
                throw new CommonException("error.instance.not.running");
            }
        }

        DevopsEnvCommandDTO devopsEnvCommandDTO = devopsEnvCommandService
                .baseQueryByObject(ObjectType.INSTANCE.getType(), instanceId);
        devopsEnvCommandDTO.setCommandType(type);
        devopsEnvCommandDTO.setStatus(CommandStatus.OPERATING.getStatus());
        devopsEnvCommandDTO.setId(null);
        devopsEnvCommandDTO = devopsEnvCommandService.baseCreate(devopsEnvCommandDTO);
        updateInstanceStatus(instanceId, devopsEnvCommandDTO.getId(), InstanceStatus.OPERATIING.getStatus());


        //发送重启或停止实例的command
        Map<String, String> stopMap = new HashMap<>();
        stopMap.put(RELEASE_NAME, applicationInstanceDTO.getCode());
        String payload = gson.toJson(stopMap);
        String instanceCommandType;
        if (CommandType.RESTART.getType().equals(type)) {
            instanceCommandType = HelmType.HELM_RELEASE_START.toValue();
        } else {
            instanceCommandType = HelmType.HELM_RELEASE_STOP.toValue();
        }
        instanceCommand(payload, applicationInstanceDTO.getCode(), instanceCommandType,
                devopsEnvironmentDTO.getCode(), devopsEnvCommandDTO.getId(), devopsEnvironmentDTO.getId(), devopsEnvironmentDTO.getClusterId());
    }


    private void initInstanceOverView(ApplicationInstanceOverViewVO applicationInstanceOverViewVO, ApplicationInstanceOverViewDTO applicationInstanceOverViewDTO,
                                      Long latestVersionId) {
        EnvVersionVO envVersionVO = new EnvVersionVO(
                applicationInstanceOverViewDTO.getVersionId(),
                applicationInstanceOverViewDTO.getVersion(),
                applicationInstanceOverViewDTO.getInstanceId(),
                applicationInstanceOverViewDTO.getInstanceCode(),
                applicationInstanceOverViewDTO.getInstanceStatus());
        EnvInstanceVO envInstanceVO = new EnvInstanceVO(applicationInstanceOverViewDTO.getEnvId());
        if (applicationInstanceOverViewDTO.getVersionId().equals(latestVersionId)) {
            envVersionVO.setLatest(true);
        }
        envInstanceVO.addEnvVersionDTOS(envVersionVO);
        applicationInstanceOverViewVO.appendEnvInstanceVOS(envInstanceVO);
        if (applicationInstanceOverViewVO.getLatestVersionId().equals(applicationInstanceOverViewDTO.getVersionId())) {
            applicationInstanceOverViewVO.appendInstances(new EnvInstancesVO(
                    applicationInstanceOverViewDTO.getInstanceId(), applicationInstanceOverViewDTO.getInstanceCode(), applicationInstanceOverViewDTO.getInstanceStatus()));
        }
    }


    private void initInstanceOverViewIfNotExist(ApplicationInstanceOverViewVO applicationInstanceOverViewVO,
                                                ApplicationInstanceOverViewDTO applicationInstanceOverViewDTO) {
        EnvInstanceVO envInstanceVO = applicationInstanceOverViewVO.queryLastEnvInstanceVO();
        if (applicationInstanceOverViewVO.getLatestVersionId().equals(applicationInstanceOverViewDTO.getVersionId())) {
            applicationInstanceOverViewVO.appendInstances(new EnvInstancesVO(
                    applicationInstanceOverViewDTO.getInstanceId(), applicationInstanceOverViewDTO.getInstanceCode(), applicationInstanceOverViewDTO.getInstanceStatus()));
        }
        if (envInstanceVO.getEnvId().equals(applicationInstanceOverViewDTO.getEnvId())) {
            EnvVersionVO envVersionVO = envInstanceVO.queryLastEnvVersionVO();
            if (envVersionVO.getVersion().equals(applicationInstanceOverViewDTO.getVersion())) {
                envVersionVO.appendInstanceList(
                        applicationInstanceOverViewDTO.getInstanceId(),
                        applicationInstanceOverViewDTO.getInstanceCode(),
                        applicationInstanceOverViewDTO.getInstanceStatus());
            } else {
                envInstanceVO.addEnvVersionDTOS(new EnvVersionVO(
                        applicationInstanceOverViewDTO.getVersionId(),
                        applicationInstanceOverViewDTO.getVersion(),
                        applicationInstanceOverViewDTO.getInstanceId(),
                        applicationInstanceOverViewDTO.getInstanceCode(),
                        applicationInstanceOverViewDTO.getInstanceStatus()));
            }
        } else {
            EnvVersionVO envVersionVO = new EnvVersionVO(
                    applicationInstanceOverViewDTO.getVersionId(),
                    applicationInstanceOverViewDTO.getVersion(),
                    applicationInstanceOverViewDTO.getInstanceId(),
                    applicationInstanceOverViewDTO.getInstanceCode(),
                    applicationInstanceOverViewDTO.getInstanceStatus());
            envInstanceVO = new EnvInstanceVO(applicationInstanceOverViewDTO.getEnvId());
            if (applicationInstanceOverViewDTO.getVersionId().equals(applicationInstanceOverViewVO.getLatestVersionId())) {
                envVersionVO.setLatest(true);
            }
            envInstanceVO.addEnvVersionDTOS(envVersionVO);
            applicationInstanceOverViewVO.appendEnvInstanceVOS(envInstanceVO);
        }
    }


    private ApplicationDTO createApplication(ApplicationRemoteDeployVO applicationRemoteDeployVO) {
        String code = applicationRemoteDeployVO.getApplicationRemoteVO().getCode();
        String name = applicationRemoteDeployVO.getApplicationRemoteVO().getName();
        ApplicationDTO applicationDTO = applicationService.baseQueryByCodeWithNullProject(code);
        if (applicationDTO == null) {
            applicationDTO = new ApplicationDTO();
            DevopsProjectConfigDTO harborConfigDTO = createConfig(HARBOR, applicationRemoteDeployVO.getApplicationRemoteVO().getCode(), applicationRemoteDeployVO.getHarbor());
            DevopsProjectConfigDTO chartConfigDTO = createConfig(CHART, applicationRemoteDeployVO.getApplicationRemoteVO().getCode(), applicationRemoteDeployVO.getChart());
            applicationDTO.setType(applicationRemoteDeployVO.getApplicationRemoteVO().getType());
            applicationDTO.setCode(code);
            applicationDTO.setName(name);
            applicationDTO.setActive(true);
            applicationDTO.setSynchro(true);
            applicationDTO.setIsSkipCheckPermission(true);
            applicationDTO.setHarborConfigId(harborConfigDTO.getId());
            applicationDTO.setChartConfigId(chartConfigDTO.getId());
            return applicationService.baseCreate(applicationDTO);
        }
        return applicationDTO;
    }


    private ApplicationVersionDTO createVersion(ApplicationDTO applicationDTO, ApplicationVersionRemoteVO versionRemoteVO) {
        ApplicationVersionDTO versionDTO = applicationVersionService.baseQueryByAppIdAndVersion(applicationDTO.getId(), versionRemoteVO.getVersion());
        if (versionDTO == null) {
            ApplicationVersionValueDTO versionValueDTO = new ApplicationVersionValueDTO();
            versionValueDTO.setValue(versionRemoteVO.getValues());
            versionValueDTO = applicationVersionValueService.baseCreate(versionValueDTO);
            ApplicationVersionReadmeDTO versionReadmeDTO = new ApplicationVersionReadmeDTO();
            versionReadmeDTO.setReadme(versionRemoteVO.getReadMeValue());
            versionReadmeDTO = applicationVersionReadmeService.baseCreate(versionReadmeDTO);
            versionDTO = new ApplicationVersionDTO();
            versionDTO.setAppId(applicationDTO.getId());
            versionDTO.setValueId(versionValueDTO.getId());
            versionDTO.setReadmeValueId(versionReadmeDTO.getId());
            return applicationVersionService.baseCreate(versionDTO);
        }
        return versionDTO;
    }

    /**
     * 创建远程配置
     *
     * @param type
     * @param code
     * @param projectConfigVO
     * @return
     */
    private DevopsProjectConfigDTO createConfig(String type, String code, ProjectConfigVO projectConfigVO) {
        String name = code + "-" + type;
        DevopsProjectConfigDTO devopsProjectConfigDTO = devopsProjectConfigService.baseCheckByName(name);
        if (devopsProjectConfigDTO == null) {
            devopsProjectConfigDTO = new DevopsProjectConfigDTO();
            devopsProjectConfigDTO.setConfig(gson.toJson(projectConfigVO));
            devopsProjectConfigDTO.setName(name);
            devopsProjectConfigDTO.setType(type);
            return devopsProjectConfigService.baseCreate(devopsProjectConfigDTO);
        }
        return devopsProjectConfigDTO;
    }


    private void updateInstanceStatus(Long instanceId, Long commandId, String status) {
        ApplicationInstanceDTO instanceDTO = baseQuery(instanceId);
        instanceDTO.setStatus(status);
        instanceDTO.setCommandId(commandId);
        baseUpdate(instanceDTO);
    }

    private void instanceCommand(String payload, String name,
                                 String type, String namespace,
                                 Long commandId, Long envId,
                                 Long clusterId) {
        Msg msg = new Msg();
        msg.setKey("cluster:" + clusterId + ".env:" + namespace + ".envId:" + envId + ".release:" + name);
        msg.setType(type);
        msg.setPayload(payload);
        msg.setCommandId(commandId);
        commandSender.sendMsg(msg);
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

    private C7nHelmRelease getC7NHelmRelease(String code, String repository, String appCode,
                                             String version, String deployValue,
                                             Long deployVersionId, String secretName) {
        C7nHelmRelease c7nHelmRelease = new C7nHelmRelease();
        c7nHelmRelease.getMetadata().setName(code);
        c7nHelmRelease.getSpec().setRepoUrl(repository);
        c7nHelmRelease.getSpec().setChartName(appCode);
        c7nHelmRelease.getSpec().setChartVersion(version);
        if (secretName != null) {
            c7nHelmRelease.getSpec().setImagePullSecrets(Arrays.asList(new ImagePullSecret(secretName)));
        }
        c7nHelmRelease.getSpec().setValues(
                getReplaceResult(applicationVersionService.baseQueryValue(deployVersionId),
                        deployValue).getDeltaYaml().trim());
        return c7nHelmRelease;
    }


    private String getDeployTime(Long diff) {
        float num = (float) diff / (60 * 1000);
        DecimalFormat df = new DecimalFormat("0.00");
        return df.format(num);
    }


    private ApplicationInstanceDTO initApplicationInstanceDTO(ApplicationDeployVO applicationDeployVO) {

        ApplicationInstanceDTO applicationInstanceDTO = new ApplicationInstanceDTO();
        applicationInstanceDTO.setAppId(applicationDeployVO.getAppId());
        applicationInstanceDTO.setEnvId(applicationDeployVO.getEnvironmentId());
        applicationInstanceDTO.setStatus(InstanceStatus.OPERATIING.getStatus());
        applicationDeployVO.setValueId(applicationDeployVO.getValueId());
        if (applicationDeployVO.getType().equals(UPDATE)) {
            ApplicationInstanceDTO oldApplicationInstanceDTO = baseQuery(
                    applicationDeployVO.getAppInstanceId());
            applicationInstanceDTO.setCode(oldApplicationInstanceDTO.getCode());
            applicationInstanceDTO.setId(applicationDeployVO.getAppInstanceId());
        }
        return applicationInstanceDTO;
    }

    private DevopsEnvCommandDTO initDevopsEnvCommandDTO(ApplicationDeployVO applicationDeployVO) {
        DevopsEnvCommandDTO devopsEnvCommandDTO = new DevopsEnvCommandDTO();
        switch (applicationDeployVO.getType()) {
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
        devopsEnvCommandDTO.setObjectVersionId(applicationDeployVO.getAppVersionId());
        devopsEnvCommandDTO.setObject(ObjectType.INSTANCE.getType());
        devopsEnvCommandDTO.setStatus(CommandStatus.OPERATING.getStatus());
        return devopsEnvCommandDTO;
    }

    private DevopsEnvCommandValueDTO initDevopsEnvCommandValueDTO
            (ApplicationDeployVO applicationDeployVO) {
        DevopsEnvCommandValueDTO devopsEnvCommandValueDTO = new DevopsEnvCommandValueDTO();
        devopsEnvCommandValueDTO.setValue(applicationDeployVO.getValues());
        return devopsEnvCommandValueDTO;
    }


    private ApplicationInstanceDTO restartDeploy(DevopsEnvironmentDTO
                                                         devopsEnvironmentDTO, ApplicationDTO
                                                         applicationDTO, ApplicationVersionDTO
                                                         applicationVersionDTO, ApplicationInstanceDTO
                                                         applicationInstanceDTO, DevopsEnvCommandValueDTO
                                                         devopsEnvCommandValueDTO, String secretCode) {
        DevopsEnvCommandDTO devopsEnvCommandDTO;
        baseUpdate(applicationInstanceDTO);
        applicationInstanceDTO = baseQuery(applicationInstanceDTO.getId());
        devopsEnvCommandDTO = devopsEnvCommandService.baseQuery(applicationInstanceDTO.getCommandId());
        devopsEnvCommandDTO.setId(null);
        devopsEnvCommandDTO.setCommandType(CommandType.UPDATE.getType());
        devopsEnvCommandDTO.setStatus(CommandStatus.OPERATING.getStatus());
        agentCommandService.deploy(applicationDTO, applicationVersionDTO, applicationInstanceDTO.getCode(), devopsEnvironmentDTO,
                devopsEnvCommandValueDTO.getValue(), devopsEnvCommandService.baseCreate(devopsEnvCommandDTO).getId(), secretCode);
        return applicationInstanceDTO;
    }

    private String getSecret(ApplicationDTO applicationDTO, String secretCode, DevopsEnvironmentDTO devopsEnvironmentDTO) {
        //如果应用绑定了私有镜像库,则处理secret
        if (applicationDTO.getHarborConfigId() != null) {
            DevopsProjectConfigDTO devopsProjectConfigDTO = devopsProjectConfigService.baseQuery(applicationDTO.getHarborConfigId());
            ProjectConfigVO projectConfigVO = gson.fromJson(devopsProjectConfigDTO.getConfig(), ProjectConfigVO.class);
            if (projectConfigVO.getPrivate() != null) {
                DevopsRegistrySecretDTO devopsRegistrySecretDTO = devopsRegistrySecretService.baseQueryByEnvAndId(devopsEnvironmentDTO.getCode(), devopsProjectConfigDTO.getId());
                if (devopsRegistrySecretDTO == null) {
                    //当配置在当前环境下没有创建过secret.则新增secret信息，并通知k8s创建secret
                    List<DevopsRegistrySecretDTO> devopsRegistrySecretDTOS = devopsRegistrySecretService.baseListByConfig(devopsProjectConfigDTO.getId());
                    if (devopsRegistrySecretDTOS.isEmpty()) {
                        secretCode = String.format("%s%s%s%s", "registry-secret-", devopsProjectConfigDTO.getId(), "-", GenerateUUID.generateUUID().substring(0, 5));
                    } else {
                        secretCode = devopsRegistrySecretDTOS.get(0).getSecretCode();
                    }
                    devopsRegistrySecretDTO = new DevopsRegistrySecretDTO(devopsEnvironmentDTO.getId(), devopsProjectConfigDTO.getId(), devopsEnvironmentDTO.getCode(), secretCode, devopsProjectConfigDTO.getConfig());
                    devopsRegistrySecretService.baseCreate(devopsRegistrySecretDTO);
                    agentCommandService.operateSecret(devopsEnvironmentDTO.getClusterId(), devopsEnvironmentDTO.getCode(), secretCode, projectConfigVO, CREATE);
                } else {
                    //判断如果某个配置有发生过修改，则需要修改secret信息，并通知k8s更新secret
                    if (!devopsRegistrySecretDTO.getSecretDetail().equals(gson.toJson(devopsProjectConfigDTO.getConfig()))) {
                        devopsRegistrySecretDTO.setSecretDetail(gson.toJson(devopsProjectConfigDTO.getConfig()));
                        devopsRegistrySecretService.baseUpdate(devopsRegistrySecretDTO);
                        agentCommandService.operateSecret(devopsEnvironmentDTO.getClusterId(), devopsEnvironmentDTO.getCode(), devopsRegistrySecretDTO.getSecretCode(), projectConfigVO, UPDATE);
                    } else {
                        if (!devopsRegistrySecretDTO.getStatus()) {
                            agentCommandService.operateSecret(devopsEnvironmentDTO.getClusterId(), devopsEnvironmentDTO.getCode(), devopsRegistrySecretDTO.getSecretCode(), projectConfigVO, UPDATE);
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
                IamUserDTO iamUserDTO = iamServiceClientOperator.queryUserByUserId(deployDTO.getCreatedBy());
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
