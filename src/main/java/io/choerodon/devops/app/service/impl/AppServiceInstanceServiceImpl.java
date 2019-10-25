package io.choerodon.devops.app.service.impl;


import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.*;
import java.util.stream.Collectors;

import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.gson.Gson;
import org.apache.commons.lang.StringUtils;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.core.io.InputStreamResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

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
import io.choerodon.devops.infra.enums.*;
import io.choerodon.devops.infra.feign.operator.BaseServiceClientOperator;
import io.choerodon.devops.infra.feign.operator.GitlabServiceClientOperator;
import io.choerodon.devops.infra.gitops.ResourceConvertToYamlHandler;
import io.choerodon.devops.infra.gitops.ResourceFileCheckHandler;
import io.choerodon.devops.infra.handler.ClusterConnectionHandler;
import io.choerodon.devops.infra.mapper.AppServiceInstanceMapper;
import io.choerodon.devops.infra.mapper.DevopsEnvAppServiceMapper;
import io.choerodon.devops.infra.mapper.PipelineAppServiceDeployMapper;
import io.choerodon.devops.infra.util.*;


/**
 * Created by Zenger on 2018/4/12.
 */
@Service
public class AppServiceInstanceServiceImpl implements AppServiceInstanceService {
    private static final String CREATE = "create";
    private static final String UPDATE = "update";
    private static final String CHOERODON = "choerodon-test";
    private static final String HARBOR = "harbor";
    private static final String AUTHTYPE = "pull";
    private static final String MANUAL = "manual";
    private static final String APP_SERVICE = "appService";
    private static final String HELM_RELEASE = "C7NHelmRelease";
    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(AppServiceInstanceServiceImpl.class);
    private static final String MASTER = "master";
    private static final String YAML_SUFFIX = ".yaml";
    private static final String RELEASE_PREFIX = "release-";
    private static final String FILE_SEPARATOR = "file.separator";
    private static final String C7NHELM_RELEASE = "C7NHelmRelease";
    private static final String RELEASE_NAME = "ReleaseName";
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
    private DevopsIngressService devopsIngressService;
    @Autowired
    private DevopsServiceService devopsServiceService;
    @Autowired
    private DevopsDeployRecordService devopsDeployRecordService;
    @Autowired
    private PipelineAppServiceDeployMapper pipelineAppServiceDeployMapper;

    @Override
    public AppServiceInstanceInfoVO queryInfoById(Long instanceId) {
        AppServiceInstanceInfoDTO appServiceInstanceInfoDTO = appServiceInstanceMapper.queryInfoById(instanceId);
        if (appServiceInstanceInfoDTO == null) {
            return null;
        }
        AppServiceInstanceInfoVO appServiceInstanceInfoVO = new AppServiceInstanceInfoVO();
        List<Long> updatedEnv = clusterConnectionHandler.getUpdatedClusterList();
        BeanUtils.copyProperties(appServiceInstanceInfoDTO, appServiceInstanceInfoVO);
        appServiceInstanceInfoVO.setConnect(updatedEnv.contains(appServiceInstanceInfoDTO.getClusterId()));
        return appServiceInstanceInfoVO;
    }

    @Override
    public PageInfo<AppServiceInstanceInfoVO> pageInstanceInfoByOptions(Long projectId, Long envId, PageRequest pageRequest, String params) {
        Map<String, Object> maps = TypeUtil.castMapParams(params);
        PageInfo<AppServiceInstanceInfoVO> pageInfo = ConvertUtils.convertPage(PageHelper.startPage(pageRequest.getPage(), pageRequest.getSize(), PageRequestUtil.getOrderBy(pageRequest))
                        .doSelectPageInfo(() -> appServiceInstanceMapper.listInstanceInfoByEnvAndOptions(
                                envId, TypeUtil.cast(maps.get(TypeUtil.SEARCH_PARAM)), TypeUtil.cast(maps.get(TypeUtil.PARAMS)))),
                AppServiceInstanceInfoVO.class);
        List<Long> updatedEnv = clusterConnectionHandler.getUpdatedClusterList();
        pageInfo.getList().forEach(appServiceInstanceInfoVO -> {
            AppServiceDTO appServiceDTO = applicationService.baseQuery(appServiceInstanceInfoVO.getAppServiceId());
            appServiceInstanceInfoVO.setAppServiceType(applicationService.checkAppServiceType(projectId,appServiceDTO));
            appServiceInstanceInfoVO.setConnect(updatedEnv.contains(appServiceInstanceInfoVO.getClusterId()));
               }
        );
        return pageInfo;
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
        List<DeployDTO> deployDTOS = baseListDeployFrequency(projectId, envIds, appServiceId, startTime, endTime);

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
        String secretCode = getSecret(applicationDTO, appServiceDeployVO.getAppServiceVersionId(), devopsEnvironmentDTO);

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

    /**
     * 创建或更新实例
     * 特别说明，此处的事务的propagation设置为{@link Propagation#REQUIRES_NEW} 是因为直接使用外层的事务会导致：
     * 当外层捕获这个方法中抛出的异常进行相应的数据库记录状态回写会被回滚，以至于外层无法在实例操作失败后记录失败
     * 的状态，因为这个事务被切面设置为 rollbackOnly 了。除非外层再次开启一个新的事务对相应操作状态进行更新。权衡
     * 之后在此方法的事务从默认事务传播级别{@link Propagation#REQUIRED} 改成 {@link Propagation#REQUIRES_NEW}
     *
     * @param appServiceDeployVO 部署信息
     * @param isFromPipeline     是否是从流水线发起的部署
     * @return 部署后实例信息
     */
    @Override
    @Saga(code = SagaTopicCodeConstants.DEVOPS_CREATE_INSTANCE,
            description = "Devops创建实例", inputSchemaClass = InstanceSagaPayload.class)
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
    public AppServiceInstanceVO createOrUpdate(AppServiceDeployVO appServiceDeployVO, boolean isFromPipeline) {

        DevopsEnvironmentDTO devopsEnvironmentDTO = devopsEnvironmentService.baseQueryById(appServiceDeployVO.getEnvironmentId());

        UserAttrDTO userAttrDTO = userAttrService.baseQueryById(TypeUtil.objToLong(GitUserNameUtil.getUserId()));

        //校验环境相关信息
        devopsEnvironmentService.checkEnv(devopsEnvironmentDTO, userAttrDTO);

        //校验values
        FileUtil.checkYamlFormat(appServiceDeployVO.getValues());

        AppServiceDTO appServiceDTO = applicationService.baseQuery(appServiceDeployVO.getAppServiceId());
        AppServiceVersionDTO appServiceVersionDTO =
                appServiceVersionService.baseQuery(appServiceDeployVO.getAppServiceVersionId());

        //初始化ApplicationInstanceDTO,DevopsEnvCommandDTO,DevopsEnvCommandValueDTO
        AppServiceInstanceDTO appServiceInstanceDTO = initApplicationInstanceDTO(appServiceDeployVO);
        DevopsEnvCommandDTO devopsEnvCommandDTO = initDevopsEnvCommandDTO(appServiceDeployVO);
        DevopsEnvCommandValueDTO devopsEnvCommandValueDTO = initDevopsEnvCommandValueDTO(appServiceDeployVO);

        //获取部署实例时授权secret的code
        String secretCode = getSecret(appServiceDTO, appServiceDeployVO.getAppServiceVersionId(), devopsEnvironmentDTO);

        // 初始化自定义实例名
        String code;
        if (appServiceDeployVO.getType().equals(CREATE)) {
            if (appServiceDeployVO.getInstanceName() == null || appServiceDeployVO.getInstanceName().trim().equals("")) {
                code = String.format("%s-%s", appServiceDTO.getCode(), GenerateUUID.generateUUID().substring(0, 5));
            } else {
                checkNameInternal(appServiceDeployVO.getInstanceName(), appServiceDeployVO.getEnvironmentId(), isFromPipeline);
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

        //更新时候，如果isNotChange的值为true，则直接return,否则走操作gitops库文件逻辑
        if (!appServiceDeployVO.getIsNotChange()) {
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

            //插入部署记录
            if (!isFromPipeline) {
                DevopsDeployRecordDTO devopsDeployRecordDTO = new DevopsDeployRecordDTO(devopsEnvironmentDTO.getProjectId(), MANUAL, devopsEnvCommandDTO.getId(), devopsEnvironmentDTO.getId().toString(), devopsEnvCommandDTO.getCreationDate());
                devopsDeployRecordService.baseCreate(devopsDeployRecordDTO);
            }

            appServiceDeployVO.setInstanceId(appServiceInstanceDTO.getId());
            appServiceDeployVO.setInstanceName(code);
            if (appServiceDeployVO.getDevopsServiceReqVO() != null) {
                appServiceDeployVO.getDevopsServiceReqVO().setDevopsIngressVO(appServiceDeployVO.getDevopsIngressVO());
            }
            InstanceSagaPayload instanceSagaPayload = new InstanceSagaPayload(devopsEnvironmentDTO.getProjectId(), userAttrDTO.getGitlabUserId(), secretCode, appServiceInstanceDTO.getCommandId().intValue());
            instanceSagaPayload.setApplicationDTO(appServiceDTO);
            instanceSagaPayload.setAppServiceVersionDTO(appServiceVersionDTO);
            instanceSagaPayload.setAppServiceDeployVO(appServiceDeployVO);
            instanceSagaPayload.setDevopsEnvironmentDTO(devopsEnvironmentDTO);
            instanceSagaPayload.setDevopsIngressVO(appServiceDeployVO.getDevopsIngressVO());
            instanceSagaPayload.setDevopsServiceReqVO(appServiceDeployVO.getDevopsServiceReqVO());

            producer.apply(
                    StartSagaBuilder
                            .newBuilder()
                            .withLevel(ResourceLevel.PROJECT)
                            .withRefType("env")
                            .withSagaCode(SagaTopicCodeConstants.DEVOPS_CREATE_INSTANCE),
                    builder -> builder
                            .withPayloadAndSerialize(instanceSagaPayload)
                            .withRefId(devopsEnvironmentDTO.getId().toString()));


        }


        return ConvertUtils.convertObject(appServiceInstanceDTO, AppServiceInstanceVO.class);
    }

    /**
     * 为环境和应用创建关联关系如果不存在
     *
     * @param appServiceId 应用id
     * @param envId        环境id
     */
    private void createEnvAppRelationShipIfNon(Long appServiceId, Long envId) {
        DevopsEnvAppServiceDTO devopsEnvAppServiceDTO = new DevopsEnvAppServiceDTO();
        devopsEnvAppServiceDTO.setAppServiceId(appServiceId);
        devopsEnvAppServiceDTO.setEnvId(envId);
        devopsEnvAppServiceMapper.insertIgnore(devopsEnvAppServiceDTO);
    }

    @Override
    public void createInstanceBySaga(InstanceSagaPayload instanceSagaPayload) {
        //更新实例的时候判断当前容器目录下是否存在环境对应的GitOps文件目录，不存在则克隆
        String filePath = null;
        try {
            if (instanceSagaPayload.getAppServiceDeployVO().getType().equals(UPDATE)) {
                filePath = clusterConnectionHandler.handDevopsEnvGitRepository(instanceSagaPayload.getProjectId(), instanceSagaPayload.getDevopsEnvironmentDTO().getCode(), instanceSagaPayload.getDevopsEnvironmentDTO().getEnvIdRsa());
            }
        } catch (Exception ex) {
            String exceptionContent = LogUtil.readContentOfThrowable(ex);
            LOGGER.info("Failed to clone repository, the ex is {}", exceptionContent);
            AppServiceInstanceDTO appServiceInstanceDTO = baseQuery(instanceSagaPayload.getAppServiceDeployVO().getInstanceId());
            appServiceInstanceDTO.setStatus(CommandStatus.FAILED.getStatus());
            baseUpdate(appServiceInstanceDTO);
            DevopsEnvCommandDTO devopsEnvCommandDTO = devopsEnvCommandService.baseQuery(appServiceInstanceDTO.getCommandId());
            devopsEnvCommandDTO.setStatus(CommandStatus.FAILED.getStatus());
            devopsEnvCommandDTO.setError(LogUtil.cutOutString("Clone repository failed. The exception is: " + exceptionContent, 5000));
            devopsEnvCommandService.baseUpdate(devopsEnvCommandDTO);
            return;
        }

        try {
            //在gitops库处理instance文件
            ResourceConvertToYamlHandler<C7nHelmRelease> resourceConvertToYamlHandler = new ResourceConvertToYamlHandler<>();
            resourceConvertToYamlHandler.setType(getC7NHelmRelease(
                    instanceSagaPayload.getAppServiceDeployVO().getInstanceName(), instanceSagaPayload.getAppServiceVersionDTO().getRepository(), instanceSagaPayload.getCommandId(), instanceSagaPayload.getApplicationDTO().getCode(), instanceSagaPayload.getAppServiceVersionDTO().getVersion(), instanceSagaPayload.getAppServiceDeployVO().getValues(), instanceSagaPayload.getAppServiceDeployVO().getAppServiceVersionId(), instanceSagaPayload.getSecretCode()));

            resourceConvertToYamlHandler.operationEnvGitlabFile(
                    RELEASE_PREFIX + instanceSagaPayload.getAppServiceDeployVO().getInstanceName(),
                    instanceSagaPayload.getDevopsEnvironmentDTO().getGitlabEnvProjectId().intValue(),
                    instanceSagaPayload.getAppServiceDeployVO().getType(),
                    instanceSagaPayload.getGitlabUserId(),
                    instanceSagaPayload.getAppServiceDeployVO().getInstanceId(), C7NHELM_RELEASE, null, false, instanceSagaPayload.getDevopsEnvironmentDTO().getId(), filePath);

            //创建实例时，如果选择了创建网络
            if (instanceSagaPayload.getDevopsServiceReqVO() != null) {
                instanceSagaPayload.getDevopsServiceReqVO().setAppServiceId(instanceSagaPayload.getApplicationDTO().getId());
                devopsServiceService.create(instanceSagaPayload.getDevopsEnvironmentDTO().getProjectId(), instanceSagaPayload.getDevopsServiceReqVO());
            }

        } catch (Exception e) {
            //有异常更新实例以及command的状态
            AppServiceInstanceDTO appServiceInstanceDTO = baseQuery(instanceSagaPayload.getAppServiceDeployVO().getInstanceId());
            DevopsEnvFileResourceDTO devopsEnvFileResourceDTO = devopsEnvFileResourceService
                    .baseQueryByEnvIdAndResourceId(instanceSagaPayload.getDevopsEnvironmentDTO().getId(), appServiceInstanceDTO.getId(), HELM_RELEASE);
            filePath = devopsEnvFileResourceDTO == null ? RELEASE_PREFIX + appServiceInstanceDTO.getCode() + YAML_SUFFIX : devopsEnvFileResourceDTO.getFilePath();
            // 这里只考虑了创建失败的情况，这说明是真的创建失败，而不是gitlab超时
            if (CREATE.equals(instanceSagaPayload.getAppServiceDeployVO().getType()) && !gitlabServiceClientOperator.getFile(TypeUtil.objToInteger(instanceSagaPayload.getDevopsEnvironmentDTO().getGitlabEnvProjectId()), MASTER,
                    filePath)) {
                String exceptionContent = LogUtil.readContentOfThrowable(e);
                LOGGER.info("Exception occurred when creating instance by saga.  Now the ex is {}", exceptionContent);
                appServiceInstanceDTO.setStatus(CommandStatus.FAILED.getStatus());
                baseUpdate(appServiceInstanceDTO);
                DevopsEnvCommandDTO devopsEnvCommandDTO = devopsEnvCommandService.baseQuery(appServiceInstanceDTO.getCommandId());
                devopsEnvCommandDTO.setStatus(CommandStatus.FAILED.getStatus());
                devopsEnvCommandDTO.setError(LogUtil.cutOutString("create or update gitOps file failed! The exception is: " + exceptionContent, 5000));
                devopsEnvCommandService.baseUpdate(devopsEnvCommandDTO);
                LOGGER.info("Successfully update the status of instance with name {} to failed after exception occurred.", appServiceInstanceDTO.getCode());
            } else {
                // 更新的超时情况暂未处理
                throw e;
            }
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
        appServiceInstanceRepVO.setAppServiceId(appServiceInstanceDTO.getAppServiceId());
        appServiceInstanceRepVO.setEnvId(appServiceInstanceDTO.getEnvId());
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
        DevopsDeployRecordDTO devopsDeployRecordDTO = new DevopsDeployRecordDTO(devopsEnvironmentDTO.getProjectId(), MANUAL, devopsEnvCommandDTO.getId(), devopsEnvironmentDTO.getId().toString(), devopsEnvCommandDTO.getCreationDate());
        devopsDeployRecordService.baseCreate(devopsDeployRecordDTO);


        return ConvertUtils.convertObject(appServiceInstanceDTO, AppServiceInstanceVO.class);
    }


    @Override
    public List<RunningInstanceVO> listRunningInstance(Long projectId, Long appServiceId, Long appServiceVersionId, Long envId) {
        return ConvertUtils.convertList(appServiceInstanceMapper.listApplicationInstanceCode(
                projectId, envId, appServiceVersionId, appServiceId), RunningInstanceVO.class);
    }

    @Override
    public List<RunningInstanceVO> listByAppIdAndEnvId(Long projectId, Long appServiceId, Long envId) {
        return ConvertUtils.convertList(appServiceInstanceMapper.listRunningAndFailedInstance(projectId, envId, appServiceId),
                RunningInstanceVO.class);
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
        String secretCode = getSecret(applicationDTO, appServiceVersionDTO.getId(), devopsEnvironmentDTO);

        //插入部署记录
        DevopsDeployRecordDTO devopsDeployRecordDTO = new DevopsDeployRecordDTO(devopsEnvironmentDTO.getProjectId(), MANUAL, devopsEnvCommandDTO.getId(), devopsEnvironmentDTO.getId().toString(), devopsEnvCommandDTO.getCreationDate());
        devopsDeployRecordService.baseCreate(devopsDeployRecordDTO);


        AppServiceDeployVO appServiceDeployVO = new AppServiceDeployVO();
        appServiceDeployVO.setInstanceId(appServiceInstanceDTO.getId());
        appServiceDeployVO.setValues(value);
        appServiceDeployVO.setType(UPDATE);
        appServiceDeployVO.setAppServiceVersionId(appServiceVersionDTO.getId());
        appServiceDeployVO.setInstanceName(appServiceInstanceDTO.getCode());
        InstanceSagaPayload instanceSagaPayload = new InstanceSagaPayload(devopsEnvironmentDTO.getProjectId(), userAttrDTO.getGitlabUserId(), secretCode, devopsEnvCommandDTO.getId().intValue());
        instanceSagaPayload.setApplicationDTO(applicationDTO);
        instanceSagaPayload.setAppServiceVersionDTO(appServiceVersionDTO);
        instanceSagaPayload.setAppServiceDeployVO(appServiceDeployVO);
        instanceSagaPayload.setDevopsEnvironmentDTO(devopsEnvironmentDTO);

        //目前重新部署也走gitops逻辑
        producer.apply(
                StartSagaBuilder
                        .newBuilder()
                        .withLevel(ResourceLevel.PROJECT)
                        .withRefType("env")
                        .withSagaCode(SagaTopicCodeConstants.DEVOPS_CREATE_INSTANCE),
                builder -> builder
                        .withPayloadAndSerialize(instanceSagaPayload)
                        .withRefId(devopsEnvironmentDTO.getId().toString()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteInstance(Long instanceId) {
        AppServiceInstanceDTO appServiceInstanceDTO = baseQuery(instanceId);

        if (appServiceInstanceDTO == null) {
            return;
        }

        DevopsEnvironmentDTO devopsEnvironmentDTO = devopsEnvironmentService.baseQueryById(appServiceInstanceDTO.getEnvId());
        UserAttrDTO userAttrDTO = userAttrService.baseQueryById(TypeUtil.objToLong(GitUserNameUtil.getUserId()));

        // 校验环境相关信息
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
            appServiceInstanceMapper.deleteByPrimaryKey(instanceId);
            devopsDeployRecordService.deleteRelatedRecordOfInstance(instanceId);
            appServiceInstanceMapper.deleteInstanceRelInfo(instanceId);
            if (gitlabServiceClientOperator.getFile(TypeUtil.objToInteger(devopsEnvironmentDTO.getGitlabEnvProjectId()), MASTER,
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
            if (!gitlabServiceClientOperator.getFile(TypeUtil.objToInteger(devopsEnvironmentDTO.getGitlabEnvProjectId()), MASTER,
                    devopsEnvFileResourceDTO.getFilePath())) {
                appServiceInstanceMapper.deleteByPrimaryKey(instanceId);
                devopsDeployRecordService.deleteRelatedRecordOfInstance(instanceId);
                appServiceInstanceMapper.deleteInstanceRelInfo(instanceId);
                devopsEnvFileResourceService.baseDeleteById(devopsEnvFileResourceDTO.getId());
                return;
            }
        }

        //如果对象所在文件只有一个对象，则直接删除文件,否则把对象从文件中去掉，更新文件
        List<DevopsEnvFileResourceDTO> devopsEnvFileResourceES = devopsEnvFileResourceService
                .baseQueryByEnvIdAndPath(devopsEnvironmentDTO.getId(), devopsEnvFileResourceDTO.getFilePath());
        if (devopsEnvFileResourceES.size() == 1) {
            if (gitlabServiceClientOperator.getFile(TypeUtil.objToInteger(devopsEnvironmentDTO.getGitlabEnvProjectId()), MASTER,
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

        pipelineAppDeployService.baseUpdateWithInstanceId(instanceId);

        devopsDeployRecordService.deleteRelatedRecordOfInstance(instanceId);
        appServiceInstanceMapper.deleteInstanceRelInfo(instanceId);
        appServiceInstanceMapper.deleteByPrimaryKey(instanceId);
    }


    @Override
    public void checkName(String code, Long envId) {
        checkNameInternal(code, envId, false);
    }

    /**
     * 校验实例名称格式是否符合，且是否重名
     *
     * @param code           实例code
     * @param envId          环境id
     * @param isFromPipeline 是否从流水自动部署中进行校验，如果是，不再校验流水线中的将要创建的实例名称是否存在
     */
    private void checkNameInternal(String code, Long envId, boolean isFromPipeline) {
        AppInstanceValidator.checkName(code);

        DevopsEnvironmentDTO devopsEnvironmentDTO = devopsEnvironmentService.baseQueryById(envId);
        List<Long> envIds = devopsEnvironmentService.baseListByClusterId(devopsEnvironmentDTO.getClusterId()).stream().map(DevopsEnvironmentDTO::getId).collect(Collectors.toList());
        // 这里校验集群下code唯一而不是环境下code唯一是因为helm的release是需要集群下唯一的
        if (appServiceInstanceMapper.checkCodeExist(code, envIds)) {
            throw new CommonException("error.app.instance.name.already.exist");
        }

        if (!isFromPipeline) {
            pipelineAppDeployService.baseCheckName(code, envId);
        }
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
        String absoluteFilePath = path + System.getProperty(FILE_SEPARATOR) + fileName;
        try {
            instanceValueVO = FileUtil.replaceNew(absoluteFilePath);
        } catch (Exception e) {
            FileUtil.deleteFile(absoluteFilePath);
            throw new CommonException(e.getMessage(), e);
        }
        if (instanceValueVO.getHighlightMarkers() == null) {
            instanceValueVO.setHighlightMarkers(new ArrayList<>());
        }
        instanceValueVO.setTotalLine(FileUtil.getFileTotalLine(instanceValueVO.getYaml()));
        FileUtil.deleteFile(absoluteFilePath);
        return instanceValueVO;
    }

    @Override
    public AppServiceInstanceDTO baseQueryByCodeAndEnv(String code, Long envId) {
        AppServiceInstanceDTO appServiceInstanceDTO = new AppServiceInstanceDTO();
        appServiceInstanceDTO.setCode(code);
        appServiceInstanceDTO.setEnvId(envId);
        return appServiceInstanceMapper.selectOne(appServiceInstanceDTO);
    }

    @Override
    public AppServiceInstanceDTO baseCreate(AppServiceInstanceDTO appServiceInstanceDTO) {
        if (appServiceInstanceMapper.insert(appServiceInstanceDTO) != 1) {
            throw new CommonException("error.application.instance.create");
        }
        return appServiceInstanceDTO;
    }

    @Override
    public AppServiceInstanceDTO baseQuery(Long id) {
        return appServiceInstanceMapper.selectByPrimaryKey(id);
    }

    @Override
    public void baseUpdate(AppServiceInstanceDTO appServiceInstanceDTO) {
        appServiceInstanceDTO.setObjectVersionNumber(
                appServiceInstanceMapper.selectByPrimaryKey(appServiceInstanceDTO.getId()).getObjectVersionNumber());
        if (appServiceInstanceMapper.updateByPrimaryKeySelective(appServiceInstanceDTO) != 1) {
            throw new CommonException("error.instance.update");
        }
    }

    @Override
    public void updateStatus(AppServiceInstanceDTO appServiceInstanceDTO) {
        appServiceInstanceMapper.updateStatus(appServiceInstanceDTO.getId(), appServiceInstanceDTO.getStatus());
    }

    @Override
    public List<AppServiceInstanceDTO> baseListByEnvId(Long envId) {
        AppServiceInstanceDTO appServiceInstanceDTO = new AppServiceInstanceDTO();
        appServiceInstanceDTO.setEnvId(envId);
        return appServiceInstanceMapper
                .select(appServiceInstanceDTO);
    }

    @Override
    public List<AppServiceInstanceOverViewDTO> baseListApplicationInstanceOverView(Long projectId, Long appServiceId, List<Long> envIds) {
        if (envIds != null && envIds.isEmpty()) {
            envIds = null;
        }
        return appServiceInstanceMapper.listApplicationInstanceOverView(projectId, appServiceId, envIds);
    }

    @Override
    public String baseQueryValueByInstanceId(Long instanceId) {
        return appServiceInstanceMapper.queryValueByInstanceId(instanceId);
    }

    @Override
    public List<DeployDTO> baseListDeployTime(Long projectId, Long envId, Long[] appServiceIds, Date startTime, Date endTime) {
        return appServiceInstanceMapper
                .listDeployTime(projectId, envId, appServiceIds, new java.sql.Date(startTime.getTime()),
                        new java.sql.Date(endTime.getTime()));
    }

    @Override
    public List<DeployDTO> baseListDeployFrequency(Long projectId, Long[] envIds, Long appServiceId,
                                                   Date startTime, Date endTime) {
        return appServiceInstanceMapper
                .listDeployFrequency(projectId, envIds, appServiceId, new java.sql.Date(startTime.getTime()),
                        new java.sql.Date(endTime.getTime()));
    }

    @Override
    public PageInfo<DeployDTO> basePageDeployFrequencyTable(Long projectId, PageRequest pageRequest, Long[] envIds, Long appServiceId,
                                                            Date startTime, Date endTime) {
        return PageHelper.startPage(pageRequest.getPage(), pageRequest.getSize(), PageRequestUtil.getOrderBy(pageRequest)).doSelectPageInfo(() ->
                appServiceInstanceMapper
                        .listDeployFrequency(projectId, envIds, appServiceId, new java.sql.Date(startTime.getTime()),
                                new java.sql.Date(endTime.getTime())));
    }

    @Override
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


    @Override
    public String baseGetInstanceResourceDetailJson(Long instanceId, String resourceName, ResourceType resourceType) {
        return appServiceInstanceMapper.getInstanceResourceDetailJson(instanceId, resourceName, resourceType.getType());
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

    private C7nHelmRelease getC7NHelmRelease(String code, String repository, Integer commandId, String appServiceCode,
                                             String version, String deployValue,
                                             Long deployVersionId, String secretName) {
        C7nHelmRelease c7nHelmRelease = new C7nHelmRelease();
        c7nHelmRelease.getMetadata().setName(code);
        c7nHelmRelease.getSpec().setRepoUrl(repository);
        c7nHelmRelease.getSpec().setChartName(appServiceCode);
        c7nHelmRelease.getSpec().setChartVersion(version);
        c7nHelmRelease.getSpec().setCommandId(commandId);
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
        appServiceInstanceDTO.setValueId(appServiceDeployVO.getValueId());
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

    private String getSecret(AppServiceDTO appServiceDTO, Long appServiceVersionId, DevopsEnvironmentDTO devopsEnvironmentDTO) {
        String secretCode = null;
        //如果应用绑定了私有镜像库,则处理secret
        AppServiceVersionDTO appServiceVersionDTO = appServiceVersionService.baseQuery(appServiceVersionId);
        DevopsConfigDTO devopsConfigDTO;
        if (appServiceVersionDTO.getHarborConfigId() != null) {
            devopsConfigDTO = devopsConfigService.baseQuery(appServiceVersionDTO.getHarborConfigId());
        } else {
            devopsConfigDTO = devopsConfigService.queryRealConfig(appServiceDTO.getId(), APP_SERVICE, HARBOR,AUTHTYPE);
        }
        if (devopsConfigDTO != null) {
            ConfigVO configVO = gson.fromJson(devopsConfigDTO.getConfig(), ConfigVO.class);
            if (configVO.getPrivate() != null && configVO.getPrivate()) {
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
                deployDetailTableVO.setUserUrl(iamUserDTO.getImageUrl());
                deployDetailTableVO.setUserLoginName(iamUserDTO.getLdap() ? iamUserDTO.getLoginName() : iamUserDTO.getEmail());
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
