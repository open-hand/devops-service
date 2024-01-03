package io.choerodon.devops.app.service.impl;


import static io.choerodon.devops.infra.constant.MarketConstant.APP_SHELVES_CODE;
import static io.choerodon.devops.infra.constant.MarketConstant.APP_SHELVES_NAME;
import static io.choerodon.devops.infra.constant.MiscConstants.APP_INSTANCE_DELETE_REDIS_KEY;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.annotation.Nullable;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import io.kubernetes.client.JSON;
import io.kubernetes.client.models.V1Service;
import io.kubernetes.client.models.V1beta1Ingress;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import io.choerodon.asgard.saga.annotation.Saga;
import io.choerodon.asgard.saga.producer.StartSagaBuilder;
import io.choerodon.asgard.saga.producer.TransactionalProducer;
import io.choerodon.core.convertor.ApplicationContextHelper;
import io.choerodon.core.domain.Page;
import io.choerodon.core.exception.CommonException;
import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.core.oauth.CustomUserDetails;
import io.choerodon.core.oauth.DetailsHelper;
import io.choerodon.devops.api.validator.AppServiceInstanceValidator;
import io.choerodon.devops.api.vo.*;
import io.choerodon.devops.api.vo.application.ApplicationInstanceInfoVO;
import io.choerodon.devops.api.vo.deploy.DeploySourceVO;
import io.choerodon.devops.api.vo.kubernetes.C7nHelmRelease;
import io.choerodon.devops.api.vo.kubernetes.ImagePullSecret;
import io.choerodon.devops.api.vo.kubernetes.InstanceValueVO;
import io.choerodon.devops.api.vo.kubernetes.Metadata;
import io.choerodon.devops.api.vo.market.MarketChartConfigVO;
import io.choerodon.devops.api.vo.market.MarketHarborConfigVO;
import io.choerodon.devops.api.vo.market.MarketServiceDeployObjectVO;
import io.choerodon.devops.api.vo.market.MarketServiceVO;
import io.choerodon.devops.app.eventhandler.constants.SagaTopicCodeConstants;
import io.choerodon.devops.app.eventhandler.payload.*;
import io.choerodon.devops.app.service.*;
import io.choerodon.devops.infra.constant.GitOpsConstants;
import io.choerodon.devops.infra.constant.MiscConstants;
import io.choerodon.devops.infra.dto.*;
import io.choerodon.devops.infra.dto.deploy.DevopsHzeroDeployConfigDTO;
import io.choerodon.devops.infra.dto.deploy.DevopsHzeroDeployDetailsDTO;
import io.choerodon.devops.infra.dto.iam.IamUserDTO;
import io.choerodon.devops.infra.dto.iam.ProjectDTO;
import io.choerodon.devops.infra.enums.*;
import io.choerodon.devops.infra.enums.deploy.*;
import io.choerodon.devops.infra.feign.operator.BaseServiceClientOperator;
import io.choerodon.devops.infra.feign.operator.GitlabServiceClientOperator;
import io.choerodon.devops.infra.feign.operator.MarketServiceClientOperator;
import io.choerodon.devops.infra.feign.operator.WorkFlowServiceOperator;
import io.choerodon.devops.infra.gitops.ResourceConvertToYamlHandler;
import io.choerodon.devops.infra.gitops.ResourceFileCheckHandler;
import io.choerodon.devops.infra.handler.ClusterConnectionHandler;
import io.choerodon.devops.infra.mapper.*;
import io.choerodon.devops.infra.util.*;
import io.choerodon.mybatis.pagehelper.PageHelper;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;

import org.hzero.core.base.BaseConstants;
import org.hzero.core.util.Pair;


/**
 * Created by Zenger on 2018/4/12.
 */
@Service
public class AppServiceInstanceServiceImpl implements AppServiceInstanceService {
    private static final Logger LOGGER = LoggerFactory.getLogger(AppServiceInstanceServiceImpl.class);

    private static final String CREATE = "create";
    private static final String UPDATE = "update";
    private static final String CHOERODON = "choerodon-test";
    private static final String AUTHTYPE = "pull";
    private static final String APP_SERVICE = "appService";
    private static final String HELM_RELEASE = "C7NHelmRelease";
    private static final String MASTER = "master";
    private static final String YAML_SUFFIX = ".yaml";
    private static final String RELEASE_PREFIX = "release-";
    private static final String FILE_SEPARATOR = "file.separator";
    private static final String C7NHELM_RELEASE = "C7NHelmRelease";
    private static final String RELEASE_NAME = "ReleaseName";
    private static final String NAMESPACE = "namespace";
    private static final String INSTANCE_NAME_TEMPLATE = "%s-%s";
    private static final Gson gson = new Gson();
    /**
     * 中间件chart仓库地址
     * gateway(%s)+市场应用名称(market)+下载地址(market/repo)
     */
    private static final String MIDDLEWARE_CHART_REPO_TEMPLATE = "%s/market/market/repo/";
    private static final String ERROR_APP_INSTANCE_IS_OPERATING = "error.app.instance.is.operating";

    @Value("${services.gateway.url}")
    private String gateway;

    @Autowired
    @Lazy
    private AgentCommandService agentCommandService;
    @Autowired
    @Lazy
    private ClusterConnectionHandler clusterConnectionHandler;

    @Autowired
    @Lazy
    private DevopsEnvResourceService devopsEnvResourceService;
    @Autowired
    @Lazy
    private DevopsEnvironmentService devopsEnvironmentService;
    @Autowired
    @Lazy
    private BaseServiceClientOperator baseServiceClientOperator;
    @Autowired
    @Lazy
    private AppServiceVersionService appServiceVersionService;
    @Autowired
    @Lazy
    private DevopsDeployValueService devopsDeployValueService;
    @Autowired
    private TransactionalProducer producer;
    @Autowired
    @Lazy
    private UserAttrService userAttrService;
    @Autowired
    @Lazy
    private AppServiceService applicationService;
    @Autowired
    @Lazy
    private DevopsConfigService devopsConfigService;
    @Autowired
    @Lazy
    private DevopsRegistrySecretService devopsRegistrySecretService;
    @Autowired
    @Lazy
    private DevopsEnvCommandService devopsEnvCommandService;
    @Autowired
    @Lazy
    private DevopsEnvCommandValueService devopsEnvCommandValueService;
    @Autowired
    @Lazy
    private DevopsEnvPodService devopsEnvPodService;
    @Autowired
    @Lazy
    private DevopsEnvFileResourceService devopsEnvFileResourceService;
    @Autowired
    @Lazy
    private GitlabServiceClientOperator gitlabServiceClientOperator;
    @Autowired
    @Lazy
    private ResourceFileCheckHandler resourceFileCheckHandler;
    @Autowired
    @Lazy
    private DevopsServiceService devopsServiceService;
    @Autowired
    @Lazy
    private DevopsDeployRecordService devopsDeployRecordService;

    @Autowired
    @Lazy
    private DevopsHarborUserService devopsHarborUserService;
    @Autowired
    @Lazy
    private SendNotificationService sendNotificationService;

    @Autowired
    @Lazy
    private DevopsIngressService devopsIngressService;
    @Autowired
    @Lazy
    private HarborService harborService;
    @Autowired
    @Lazy
    private PermissionHelper permissionHelper;
    @Autowired
    @Lazy
    private DevopsEnvApplicationService devopsEnvApplicationService;
    @Autowired
    @Lazy
    private MarketServiceClientOperator marketServiceClientOperator;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    @Lazy
    private DevopsHzeroDeployDetailsService devopsHzeroDeployDetailsService;
    @Autowired
    @Lazy
    private DevopsHzeroDeployConfigService devopsHzeroDeployConfigService;
    @Autowired
    @Lazy
    private WorkFlowServiceOperator workFlowServiceOperator;
    @Autowired
    @Lazy
    private DevopsDeployAppCenterService devopsDeployAppCenterService;

    @Autowired
    private AppServiceInstanceMapper appServiceInstanceMapper;
    @Autowired
    private DevopsClusterMapper devopsClusterMapper;
    @Autowired
    private DevopsClusterResourceMapper devopsClusterResourceMapper;
    @Autowired
    private DevopsPrometheusMapper devopsPrometheusMapper;
    @Autowired
    private DevopsProjectMapper devopsProjectMapper;
    /**
     * 前端传入的排序字段和Mapper文件中的字段名的映射
     */
    private static final Map<String, String> orderByFieldMap;

    static {
        Map<String, String> map = new HashMap<>();
        map.put("id", "id");
        map.put("appServiceName", "app_service_name");
        map.put("versionName", "version_name");
        map.put("code", "code");
        orderByFieldMap = Collections.unmodifiableMap(map);
    }

    @Override
    public AppServiceInstanceInfoVO queryInfoById(Long projectId, Long instanceId) {
        AppServiceInstanceInfoDTO appServiceInstanceInfoDTO = appServiceInstanceMapper.queryInfoById(instanceId);
        if (appServiceInstanceInfoDTO == null) {
            return null;
        }
        AppServiceInstanceInfoVO appServiceInstanceInfoVO = new AppServiceInstanceInfoVO();
        List<Long> updatedEnv = clusterConnectionHandler.getUpdatedClusterList();
        BeanUtils.copyProperties(appServiceInstanceInfoDTO, appServiceInstanceInfoVO);
        appServiceInstanceInfoVO.setConnect(updatedEnv.contains(appServiceInstanceInfoDTO.getClusterId()));

        // 为市场实例填充版本信息
        if (isMarket(appServiceInstanceInfoDTO.getSource()) || isMiddleware(appServiceInstanceInfoDTO.getSource())) {
            fillInformationForMarketInstance(appServiceInstanceInfoDTO, appServiceInstanceInfoVO);
        }
        appServiceInstanceInfoVO.setDevopsDeployAppCenterEnvDTO(devopsDeployAppCenterService.queryByRdupmTypeAndObjectId(RdupmTypeEnum.CHART, instanceId));

        return appServiceInstanceInfoVO;
    }

    private void fillInformationForMarketInstance(AppServiceInstanceInfoDTO appServiceInstanceInfoDTO, AppServiceInstanceInfoVO appServiceInstanceInfoVO) {
        Set<Long> deployObjectIds = new HashSet<>();
        deployObjectIds.add(appServiceInstanceInfoDTO.getCommandVersionId());
        // 这个id可能为空
        if (appServiceInstanceInfoDTO.getEffectCommandVersionId() != null) {
            deployObjectIds.add(appServiceInstanceInfoDTO.getEffectCommandVersionId());
        }
        Map<Long, MarketServiceDeployObjectVO> versions = marketServiceClientOperator.listDeployObjectsByIds(appServiceInstanceInfoDTO.getProjectId(), deployObjectIds).stream().collect(Collectors.toMap(MarketServiceDeployObjectVO::getId, Function.identity()));
        if (versions.get(appServiceInstanceInfoDTO.getCommandVersionId()) != null) {
            appServiceInstanceInfoVO.setMktAppVersionId(versions.get(appServiceInstanceInfoDTO.getCommandVersionId()).getMarketAppVersionId());
            appServiceInstanceInfoVO.setMktDeployObjectId(appServiceInstanceInfoDTO.getCommandVersionId());
            // 如果是中间件，直接以应用版本作为生效版本
            if (isMiddleware(appServiceInstanceInfoDTO.getSource())) {
                appServiceInstanceInfoVO.setCommandVersion(versions.get(appServiceInstanceInfoDTO.getCommandVersionId()).getMarketServiceVersion());
            } else {
                appServiceInstanceInfoVO.setCommandVersion(versions.get(appServiceInstanceInfoDTO.getCommandVersionId()).getDevopsAppServiceVersion());
            }
            appServiceInstanceInfoVO.setCurrentVersionAvailable(true);
        } else {
            appServiceInstanceInfoVO.setCommandVersion("版本已被删除");
            appServiceInstanceInfoVO.setCurrentVersionAvailable(false);
        }
        if (versions.get(appServiceInstanceInfoDTO.getEffectCommandVersionId()) != null) {
            // 如果是中间件，直接以应用版本作为生效版本
            if (isMiddleware(appServiceInstanceInfoDTO.getSource())) {
                appServiceInstanceInfoVO.setEffectCommandVersion(versions.get(appServiceInstanceInfoDTO.getEffectCommandVersionId()).getMarketServiceVersion());
            } else {
                appServiceInstanceInfoVO.setEffectCommandVersion(versions.get(appServiceInstanceInfoDTO.getEffectCommandVersionId()).getDevopsAppServiceVersion());
            }
        }

        List<MarketServiceDeployObjectVO> upgradeAble = marketServiceClientOperator.queryUpgradeDeployObjects(appServiceInstanceInfoDTO.getProjectId(), appServiceInstanceInfoDTO.getAppServiceId(), appServiceInstanceInfoDTO.getCommandVersionId());
        // 这里查出的版本是包含当前的版本和最新的版本，两个版本
        // 如果只查出一个版本，但不是当前版本，就是可升级的
        if (upgradeAble.size() > 1) {
            appServiceInstanceInfoVO.setUpgradeAvailable(true);
        } else {
            appServiceInstanceInfoVO.setUpgradeAvailable(upgradeAble.size() == 1 && !appServiceInstanceInfoDTO.getCommandVersionId().equals(upgradeAble.get(0).getId()));
        }

        MarketServiceVO marketServiceVO = marketServiceClientOperator.queryMarketService(appServiceInstanceInfoDTO.getProjectId(), appServiceInstanceInfoDTO.getAppServiceId());
        if (marketServiceVO != null) {
            appServiceInstanceInfoVO.setApplicationType(marketServiceVO.getApplicationType());
        }
        appServiceInstanceInfoVO.setAppServiceName(marketServiceVO != null ? marketServiceVO.getMarketServiceName() : MiscConstants.UNKNOWN_SERVICE);
    }

    @Override
    public Page<AppServiceInstanceInfoVO> pageInstanceInfoByOptions(Long projectId, Long envId, PageRequest pageable, String params) {
        Map<String, Object> maps = TypeUtil.castMapParams(params);
        List<Long> updatedEnv = clusterConnectionHandler.getUpdatedClusterList();
        Page<AppServiceInstanceInfoVO> pageInfo = ConvertUtils.convertPage(PageHelper.doPageAndSort(PageRequestUtil.getMappedPage(pageable, orderByFieldMap), () -> appServiceInstanceMapper.listInstanceInfoByEnvAndOptions(
                        envId, TypeUtil.cast(maps.get(TypeUtil.SEARCH_PARAM)), TypeUtil.cast(maps.get(TypeUtil.PARAMS)))),
                AppServiceInstanceInfoVO.class);
        Set<Long> marketInstanceCommandVersionIds = new HashSet<>();
        Set<Long> appServiceIds = new HashSet<>();

        // 收集ids
        pageInfo.getContent().forEach(ins -> {
            if (isMarket(ins.getSource()) || isMiddleware(ins.getSource())) {
                marketInstanceCommandVersionIds.add(ins.getCommandVersionId());
            }
            appServiceIds.add(ins.getAppServiceId());
        });

        // 如果市场实例不为空，从市场服务查询版本信息
        Map<Long, MarketServiceDeployObjectVO> deployObjects;
        if (!CollectionUtils.isEmpty(marketInstanceCommandVersionIds)) {
            deployObjects = marketServiceClientOperator.listDeployObjectsByIds(projectId, marketInstanceCommandVersionIds).stream().collect(Collectors.toMap(MarketServiceDeployObjectVO::getId, Function.identity()));
        } else {
            deployObjects = Collections.emptyMap();
        }

        // 查询应用服务信息
        Map<Long, AppServiceDTO> appServices = applicationService.baseListByIds(appServiceIds).stream().collect(Collectors.toMap(AppServiceDTO::getId, Function.identity()));

        pageInfo.getContent().forEach(appServiceInstanceInfoVO -> {
                    AppServiceDTO appServiceDTO = appServices.get(appServiceInstanceInfoVO.getAppServiceId());
                    appServiceInstanceInfoVO.setAppServiceType(applicationService.checkAppServiceType(projectId, appServiceDTO == null ? null : appServiceDTO.getProjectId(), appServiceInstanceInfoVO.getSource()));
                    appServiceInstanceInfoVO.setConnect(updatedEnv.contains(appServiceInstanceInfoVO.getClusterId()));

                    // 为应用市场实例填充版本信息
                    if (isMarket(appServiceInstanceInfoVO.getSource()) || isMiddleware(appServiceInstanceInfoVO.getSource())) {
                        if (deployObjects.get(appServiceInstanceInfoVO.getCommandVersionId()) != null) {
                            MarketServiceDeployObjectVO deployObject = deployObjects.get(appServiceInstanceInfoVO.getCommandVersionId());
                            if (isMiddleware(appServiceInstanceInfoVO.getSource())) {
                                appServiceInstanceInfoVO.setCommandVersion(deployObject.getMarketServiceVersion());
                            } else {
                                appServiceInstanceInfoVO.setCommandVersion(deployObject.getDevopsAppServiceVersion());
                            }
                            appServiceInstanceInfoVO.setAppServiceName(deployObject.getMarketServiceName());
                        }
                    }
                }
        );
        return pageInfo;
    }

    @Override
    public Page<DevopsEnvPreviewInstanceVO> pageByOptions(Long projectId, PageRequest pageable,
                                                          Long envId, Long appServiceVersionId, Long appServiceId, Long instanceId, String params) {

        Page<DevopsEnvPreviewInstanceVO> devopsEnvPreviewInstanceDTOPageInfo = new Page<>();

        Map maps = gson.fromJson(params, Map.class);
        Map<String, Object> searchParamMap = TypeUtil.cast(maps.get(TypeUtil.SEARCH_PARAM));
        List<String> paramList = TypeUtil.cast(maps.get(TypeUtil.PARAMS));
        Page<AppServiceInstanceDTO> applicationInstanceDTOPageInfo = PageHelper.doPageAndSort(PageRequestUtil.simpleConvertSortForPage(pageable), () ->
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
            fillDeployValueInfo(instanceValueVO, appServiceInstanceDTO.getValueId());
            instanceValueVO.setYaml(getReplaceResult(versionValue, baseQueryValueByInstanceId(instanceId)).getYaml());
        } else {
            // 如果是创建实例,直接返回版本values
            instanceValueVO.setYaml(versionValue);
        }
        return instanceValueVO;
    }

    @Override
    public InstanceValueVO queryUpgradeValue(Long instanceId, Long appServiceVersionId) {
        AppServiceInstanceDTO appServiceInstanceDTO = baseQuery(instanceId);
        // 上次实例部署时的完整values
        String yaml = FileUtil.checkValueFormat(baseQueryValueByInstanceId(instanceId));
        // 这里不能直接用app_service_version_id字段查version的values，因为它可能为空
        String lastVersionValue = appServiceInstanceMapper.queryLastCommandVersionValueByInstanceId(instanceId);
        DevopsEnvCommandDTO devopsEnvCommandDTO = devopsEnvCommandService.baseQuery(Objects.requireNonNull(appServiceInstanceMapper.queryLastCommandId(instanceId)));

        // 上次实例部署时的values相较于上次版本的默认values的变化值
        String lastDeltaValues = getReplaceResult(Objects.requireNonNull(lastVersionValue),
                Objects.requireNonNull(yaml))
                .getDeltaYaml();

        InstanceValueVO instanceValueVO = new InstanceValueVO();
        fillDeployValueInfo(instanceValueVO, appServiceInstanceDTO.getValueId());
        // 新的版本的values值, 如果新版本id和上个版本id一致，就用之前查询的
        if (devopsEnvCommandDTO.getObjectVersionId() != null && devopsEnvCommandDTO.getObjectVersionId().equals(appServiceVersionId)) {
            instanceValueVO.setYaml(yaml);
        } else {
            // 将新的版本的values和上次部署的变化值进行合并
            instanceValueVO.setYaml(getReplaceResult(appServiceVersionService.baseQueryValue(appServiceVersionId), lastDeltaValues).getYaml());
        }
        return instanceValueVO;
    }

    @Override
    public InstanceValueVO queryUpgradeValueForMarketInstance(Long projectId, Long instanceId, Long marketDeployObjectId) {
        AppServiceInstanceDTO appServiceInstanceDTO = baseQuery(instanceId);
        CommonExAssertUtil.assertNotNull(appServiceInstanceDTO, "instance.not.exist.in.database");
        // 上次实例部署时的完整values
        String yaml = FileUtil.checkValueFormat(baseQueryValueByInstanceId(instanceId));
        DevopsEnvCommandDTO devopsEnvCommandDTO = devopsEnvCommandService.baseQuery(Objects.requireNonNull(appServiceInstanceMapper.queryLastCommandId(instanceId)));
        String lastVersionValue = marketServiceClientOperator.queryValues(projectId, devopsEnvCommandDTO.getObjectVersionId()).getValue();

        // 上次实例部署时的values相较于上次版本的默认values的变化值
        String lastDeltaValues = getReplaceResult(Objects.requireNonNull(lastVersionValue),
                Objects.requireNonNull(yaml))
                .getDeltaYaml();

        InstanceValueVO instanceValueVO = new InstanceValueVO();
        fillDeployValueInfo(instanceValueVO, appServiceInstanceDTO.getValueId());
        // 新的版本的values值, 如果新版本id和上个版本id一致，就用之前查询的

        if (devopsEnvCommandDTO.getObjectVersionId() != null && devopsEnvCommandDTO.getObjectVersionId().equals(marketDeployObjectId)) {
            instanceValueVO.setYaml(yaml);

        } else {
            // 将新的版本的values和上次部署的变化值进行合并
            instanceValueVO.setYaml(getReplaceResult(marketServiceClientOperator.queryValues(projectId, marketDeployObjectId).getValue(), lastDeltaValues).getYaml());
        }


        return instanceValueVO;
    }

    /**
     * 填充部署配置相关信息（如果有）
     *
     * @param instanceValueVO 实例values相关信息
     * @param instanceValueId 实例纪录的valueId，部署配置id，可为空
     */
    private void fillDeployValueInfo(InstanceValueVO instanceValueVO, @Nullable Long instanceValueId) {
        if (instanceValueId == null) {
            return;
        }
        DevopsDeployValueDTO devopsDeployValueDTO = devopsDeployValueService.baseQueryById(instanceValueId);
        instanceValueVO.setName(devopsDeployValueDTO.getName());
        instanceValueVO.setId(devopsDeployValueDTO.getId());
        instanceValueVO.setObjectVersionNumber(devopsDeployValueDTO.getObjectVersionNumber());
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
        if (Objects.isNull(envIds) || envIds.length == 0) {
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
    public Page<DeployDetailTableVO> pageDeployFrequencyTable(Long projectId, PageRequest pageable, Long[] envIds,
                                                              Long appServiceId, Date startTime, Date endTime) {
        if (envIds == null || envIds.length == 0) {
            return new Page<>();
        }
        Page<DeployDTO> deployDTOPageInfo = basePageDeployFrequencyTable(projectId, pageable,
                envIds, appServiceId, startTime, endTime);
        return getDeployDetailDTOS(deployDTOPageInfo);
    }


    @Override
    public Page<DeployDetailTableVO> pageDeployTimeTable(Long projectId, PageRequest pageable,
                                                         Long[] appServiceIds, Long envId,
                                                         Date startTime, Date endTime) {
        if (appServiceIds.length == 0) {
            return new Page<>();
        }
        Page<DeployDTO> deployDTOS = basePageDeployTimeTable(projectId, pageable, envId,
                appServiceIds, startTime, endTime);
        return getDeployDetailDTOS(deployDTOS);
    }


    @Override
    public void deployTestApp(Long projectId, AppServiceDeployVO appServiceDeployVO) {
        // 这里的environmentId就是集群id
        CommonExAssertUtil.assertTrue(permissionHelper.projectPermittedToCluster(appServiceDeployVO.getEnvironmentId(), projectId), MiscConstants.ERROR_OPERATING_RESOURCE_IN_OTHER_PROJECT);

        String versionValue = appServiceVersionService.baseQueryValue(appServiceDeployVO.getAppServiceVersionId());
        AppServiceDTO appServiceDTO = applicationService.baseQuery(appServiceDeployVO.getAppServiceId());

        DevopsEnvironmentDTO devopsEnvironmentDTO = new DevopsEnvironmentDTO();
        devopsEnvironmentDTO.setClusterId(appServiceDeployVO.getEnvironmentId());
        devopsEnvironmentDTO.setCode(CHOERODON);
        // 测试应用没有环境id
        String secretCode = getSecret(appServiceDTO, appServiceDeployVO.getAppServiceVersionId(), devopsEnvironmentDTO);

        AppServiceVersionDTO appServiceVersionDTO = appServiceVersionService.baseQuery(appServiceDeployVO.getAppServiceVersionId());
        FileUtil.checkYamlFormat(appServiceDeployVO.getValues());
        String deployValue = getReplaceResult(versionValue,
                appServiceDeployVO.getValues()).getDeltaYaml().trim();
        agentCommandService.deployTestApp(appServiceDTO, appServiceVersionDTO, appServiceDeployVO.getInstanceName(), secretCode, appServiceDeployVO.getEnvironmentId(), deployValue);
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
    public void operationPodCount(Long projectId, String kind, String name, Long envId, Long count, boolean workload) {
        DevopsEnvironmentDTO devopsEnvironmentDTO = permissionHelper.checkEnvBelongToProject(projectId, envId);

        UserAttrDTO userAttrDTO = userAttrService.baseQueryById(TypeUtil.objToLong(GitUserNameUtil.getUserId()));

        //校验环境相关信息
        devopsEnvironmentService.checkEnv(devopsEnvironmentDTO, userAttrDTO);

        //不能减少到0
        if (!workload && count == 0) {
            return;
        }

        agentCommandService.operatePodCount(kind, name, devopsEnvironmentDTO.getCode(), devopsEnvironmentDTO.getClusterId(), count);
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
        AppServiceInstanceDTO appServiceInstanceDTO = appServiceInstanceMapper.selectByPrimaryKey(instanceId);
        if (appServiceInstanceDTO == null) {
            return null;
        }

        // 获取相关的pod
        List<DevopsEnvPodVO> devopsEnvPodDTOS = devopsEnvResourceService.listPodResourceByInstanceId(instanceId);

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
                        .peek(daemonSetVO -> daemonSetVO.setDevopsEnvPodVOS(
                                filterPodsAssociatedWithResource(devopsEnvPodDTOS, daemonSetVO.getName(), ResourceType.DAEMONSET.getType())
                        ))
                        .collect(Collectors.toList())
        );

        // 关联其pod并设置statefulSet
        devopsEnvResourceVO.setStatefulSetVOS(
                devopsEnvResourceVO.getStatefulSetVOS()
                        .stream()
                        .peek(statefulSetVO -> statefulSetVO.setDevopsEnvPodVOS(
                                filterPodsAssociatedWithResource(devopsEnvPodDTOS, statefulSetVO.getName(), ResourceType.STATEFULSET.getType())))
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
    public AppServiceInstanceVO createOrUpdate(@Nullable Long projectId, AppServiceDeployVO appServiceDeployVO, boolean isFromPipeline) {
        // 校验在应用中心的名称、code是否已存在
        devopsDeployAppCenterService.checkNameAndCodeUniqueAndThrow(appServiceDeployVO.getEnvironmentId(), RdupmTypeEnum.CHART.value(), appServiceDeployVO.getInstanceId(), appServiceDeployVO.getAppName(), appServiceDeployVO.getAppCode());

        DevopsEnvironmentDTO devopsEnvironmentDTO = devopsEnvironmentService.baseQueryById(appServiceDeployVO.getEnvironmentId());

        // 自动部署传入的项目id是空的, 不用校验
        if (projectId != null) {
            CommonExAssertUtil.assertTrue(projectId.equals(devopsEnvironmentDTO.getProjectId()), MiscConstants.ERROR_OPERATING_RESOURCE_IN_OTHER_PROJECT);
        }

        UserAttrDTO userAttrDTO = userAttrService.baseQueryById(TypeUtil.objToLong(GitUserNameUtil.getUserId()));

        //校验环境相关信息
        devopsEnvironmentService.checkEnv(devopsEnvironmentDTO, userAttrDTO);

        //校验values
        FileUtil.checkYamlFormat(appServiceDeployVO.getValues());

        AppServiceDTO appServiceDTO = applicationService.baseQuery(appServiceDeployVO.getAppServiceId());

        if (appServiceDTO == null) {
            throw new CommonException("error.app.service.not.exist");
        }

        if (!Boolean.TRUE.equals(appServiceDTO.getActive())) {
            throw new CommonException("error.app.service.disabled");
        }

        AppServiceVersionDTO appServiceVersionDTO =
                appServiceVersionService.baseQuery(appServiceDeployVO.getAppServiceVersionId());
        CommonExAssertUtil.assertNotNull(appServiceVersionDTO, "error.version.id.not.exist", appServiceDeployVO.getAppServiceVersionId());
        if (appServiceDeployVO.getType().equals(UPDATE)) {
            checkInstanceConsistent(appServiceDeployVO.getInstanceId(), appServiceVersionDTO.getAppServiceId());
        }
        appServiceDeployVO.setAppServiceId(appServiceVersionDTO.getAppServiceId());

        //初始化ApplicationInstanceDTO,DevopsEnvCommandDTO,DevopsEnvCommandValueDTO
        AppServiceInstanceDTO appServiceInstanceDTO = initApplicationInstanceDTO(appServiceDeployVO);
        DevopsEnvCommandDTO devopsEnvCommandDTO = initDevopsEnvCommandDTO(appServiceDeployVO);
        DevopsEnvCommandValueDTO devopsEnvCommandValueDTO = initDevopsEnvCommandValueDTO(appServiceDeployVO.getValues());

        //获取部署实例时授权secret的code
        String secretCode = getSecret(appServiceDTO, appServiceDeployVO.getAppServiceVersionId(), devopsEnvironmentDTO);

        // 初始化自定义实例名
        String code;
        if (appServiceDeployVO.getType().equals(CREATE)) {
            if (appServiceDeployVO.getInstanceName() == null || appServiceDeployVO.getInstanceName().trim().equals("")) {
                code = String.format(INSTANCE_NAME_TEMPLATE, appServiceDTO.getCode(), GenerateUUID.generateUUID().substring(0, 5));
            } else {
                checkNameInternal(appServiceDeployVO.getInstanceName(), appServiceDeployVO.getEnvironmentId(), isFromPipeline);
                code = appServiceDeployVO.getInstanceName();
            }
        } else {
            code = appServiceInstanceDTO.getCode();
            //更新实例的时候校验gitops库文件是否存在,处理部署实例时，由于没有创gitops文件导致的部署失败
            resourceFileCheckHandler.check(devopsEnvironmentDTO, appServiceDeployVO.getInstanceId(), code, C7NHELM_RELEASE);

            //从未关联部署配置到关联部署配置，或者从一个部署配置关联另外一个部署配置，如果values是一样的，虽然getIsNotChange为false,但是此时也应该直接设置为isNotChange为true
            AppServiceInstanceDTO oldAppServiceInstanceDTO = baseQuery(appServiceDeployVO.getInstanceId());
            String deployValue = baseQueryValueByInstanceId(appServiceInstanceDTO.getId());
            if (appServiceDeployVO.getAppServiceVersionId().equals(oldAppServiceInstanceDTO.getAppServiceVersionId()) && deployValue.equals(appServiceDeployVO.getValues())) {
                appServiceDeployVO.setIsNotChange(true);
            }
        }
        boolean isProjectAppService = devopsEnvironmentDTO.getProjectId().equals(appServiceDTO.getProjectId());
        //插入部署记录
        ProjectDTO projectDTO = baseServiceClientOperator.queryIamProjectById(devopsEnvironmentDTO.getProjectId());
        //更新时候，如果isNotChange的值为true，则直接return,否则走操作gitops库文件逻辑
        DevopsDeployAppCenterEnvDTO devopsDeployAppCenterEnvDTO;
        if (!appServiceDeployVO.getIsNotChange()) {
            //插入应用服务与环境的关联
            ApplicationCenterEnum appSourceType = isProjectAppService ? ApplicationCenterEnum.PROJECT : ApplicationCenterEnum.SHARE;
            devopsEnvApplicationService.createEnvAppRelationShipIfNon(appServiceDeployVO.getAppServiceId(), appServiceDeployVO.getEnvironmentId(), appSourceType.value(), appServiceDTO.getCode(), appServiceDTO.getName());
            if (appServiceDeployVO.getType().equals(CREATE)) {
                appServiceInstanceDTO.setCode(code);
                appServiceInstanceDTO.setId(baseCreate(appServiceInstanceDTO).getId());
            }
            devopsEnvCommandDTO.setObjectId(appServiceInstanceDTO.getId());
            devopsEnvCommandDTO.setValueId(devopsEnvCommandValueService.baseCreate(devopsEnvCommandValueDTO).getId());
            devopsEnvCommandDTO = devopsEnvCommandService.baseCreate(devopsEnvCommandDTO);
            appServiceInstanceDTO.setCommandId(devopsEnvCommandDTO.getId());
            baseUpdate(appServiceInstanceDTO);


            appServiceDeployVO.setInstanceId(appServiceInstanceDTO.getId());
            appServiceDeployVO.setInstanceName(code);
            if (appServiceDeployVO.getDevopsServiceReqVO() != null) {
                appServiceDeployVO.getDevopsServiceReqVO().setDevopsIngressVO(appServiceDeployVO.getDevopsIngressVO());
            }
            InstanceSagaPayload instanceSagaPayload = new InstanceSagaPayload(devopsEnvironmentDTO.getProjectId(), userAttrDTO.getGitlabUserId(), secretCode, appServiceInstanceDTO.getCommandId());
            instanceSagaPayload.setApplicationDTO(appServiceDTO);
            instanceSagaPayload.setAppServiceVersionDTO(appServiceVersionDTO);
            instanceSagaPayload.setAppServiceDeployVO(appServiceDeployVO);
            instanceSagaPayload.setDevopsEnvironmentDTO(devopsEnvironmentDTO);
            instanceSagaPayload.setDevopsIngressVO(appServiceDeployVO.getDevopsIngressVO());
            instanceSagaPayload.setDevopsServiceReqVO(appServiceDeployVO.getDevopsServiceReqVO());


            if (CREATE.equals(appServiceDeployVO.getType())) {
                devopsDeployAppCenterEnvDTO = devopsDeployAppCenterService.baseCreate(appServiceDeployVO.getAppName(), appServiceDeployVO.getAppCode(), projectId, appServiceInstanceDTO.getId(), appServiceDeployVO.getEnvironmentId(), isFromPipeline ? OperationTypeEnum.PIPELINE_DEPLOY.value() : OperationTypeEnum.CREATE_APP.value(), isProjectAppService ? AppSourceType.NORMAL.getValue() : AppSourceType.SHARE.getValue(), RdupmTypeEnum.CHART.value());
            } else {
                devopsDeployAppCenterEnvDTO = devopsDeployAppCenterService.queryByEnvIdAndCode(appServiceDeployVO.getEnvironmentId(), code);
                devopsDeployAppCenterEnvDTO.setName(appServiceDeployVO.getAppName());
                devopsDeployAppCenterService.baseUpdate(devopsDeployAppCenterEnvDTO);
            }
            devopsDeployRecordService.saveRecord(
                    devopsEnvironmentDTO.getProjectId(),
                    isFromPipeline ? DeployType.AUTO : DeployType.MANUAL,
                    devopsEnvCommandDTO.getId(),
                    DeployModeEnum.ENV,
                    devopsEnvironmentDTO.getId(),
                    devopsEnvironmentDTO.getName(),
                    null,
                    DeployObjectTypeEnum.CHART,
                    appServiceDTO.getName(),
                    appServiceVersionDTO.getVersion(),
                    appServiceDeployVO.getAppName(),
                    appServiceDeployVO.getAppCode(),
                    devopsDeployAppCenterEnvDTO.getId(),
                    new DeploySourceVO(isProjectAppService ? AppSourceType.CURRENT_PROJECT : AppSourceType.SHARE, projectDTO.getName()));

            producer.apply(
                    StartSagaBuilder
                            .newBuilder()
                            .withLevel(ResourceLevel.PROJECT)
                            .withSourceId(devopsEnvironmentDTO.getProjectId())
                            .withRefType("env")
                            .withSagaCode(SagaTopicCodeConstants.DEVOPS_CREATE_INSTANCE),
                    builder -> builder
                            .withPayloadAndSerialize(instanceSagaPayload)
                            .withRefId(devopsEnvironmentDTO.getId().toString()));
        } else {
            if (CREATE.equals(appServiceDeployVO.getType())) {
                devopsDeployAppCenterEnvDTO = devopsDeployAppCenterService.baseCreate(appServiceDeployVO.getAppName(), appServiceDeployVO.getAppCode(), projectId, appServiceInstanceDTO.getId(), appServiceDeployVO.getEnvironmentId(), isFromPipeline ? OperationTypeEnum.PIPELINE_DEPLOY.value() : OperationTypeEnum.CREATE_APP.value(), isProjectAppService ? AppSourceType.NORMAL.getValue() : AppSourceType.SHARE.getValue(), RdupmTypeEnum.CHART.value());
            } else {
                devopsDeployAppCenterEnvDTO = devopsDeployAppCenterService.queryByEnvIdAndCode(appServiceDeployVO.getEnvironmentId(), code);
                devopsDeployAppCenterEnvDTO.setName(appServiceDeployVO.getAppName());
                devopsDeployAppCenterService.baseUpdate(devopsDeployAppCenterEnvDTO);
            }
        }


        AppServiceInstanceVO instanceVO = ConvertUtils.convertObject(appServiceInstanceDTO, AppServiceInstanceVO.class);
        instanceVO.setAppId(devopsDeployAppCenterEnvDTO.getId());
        return instanceVO;
    }

    @Saga(code = SagaTopicCodeConstants.DEVOPS_CREATE_MARKET_INSTANCE,
            description = "Devops创建市场实例", inputSchemaClass = MarketInstanceSagaPayload.class)
    @Override
    public AppServiceInstanceVO createOrUpdateMarketInstance(Long projectId, MarketInstanceCreationRequestVO appServiceDeployVO, Boolean saveRecord) {
        // 校验在应用中心的名称、code是否已存在
        devopsDeployAppCenterService.checkNameAndCodeUniqueAndThrow(appServiceDeployVO.getEnvironmentId(), RdupmTypeEnum.CHART.value(), appServiceDeployVO.getInstanceId(), appServiceDeployVO.getAppName(), appServiceDeployVO.getAppCode());

        //1. 查询校验环境
        DevopsEnvironmentDTO devopsEnvironmentDTO = getProjectEnvironment(projectId, appServiceDeployVO.getEnvironmentId());

        // 2.校验用户是否拥有环境权限
        UserAttrDTO userAttrDTO = userAttrService.baseQueryById(TypeUtil.objToLong(GitUserNameUtil.getUserId()));
        devopsEnvironmentService.checkEnv(devopsEnvironmentDTO, userAttrDTO);

        //3.校验valus
        FileUtil.checkYamlFormat(appServiceDeployVO.getValues());

        //4.获得市场服务
        MarketServiceVO marketServiceVO;
        //5.获得市场部署对象
        MarketServiceDeployObjectVO marketServiceDeployObjectVO = getMarketServiceDeployObjectVO(projectId, appServiceDeployVO.getMarketDeployObjectId());

        String appServiceCode;
        String appServiceName;
        if (appServiceDeployVO.getMarketAppServiceId() == null) {
            appServiceDeployVO.setMarketAppServiceId(marketServiceDeployObjectVO.getMarketServiceId());
        }
        marketServiceVO = getMarketServiceVO(projectId, appServiceDeployVO.getMarketAppServiceId(), appServiceDeployVO.getMarketDeployObjectId());
        if (AppSourceType.HZERO.getValue().equals(appServiceDeployVO.getApplicationType())) {
            appServiceCode = marketServiceVO.getMarketServiceCode();
            appServiceName = marketServiceVO.getMarketServiceName();
        } else {
            appServiceCode = marketServiceDeployObjectVO.getDevopsAppServiceCode();
            appServiceName = marketServiceDeployObjectVO.getDevopsAppServiceName();
        }


        //6.如果是跟新校验前后的版本属于同一个服务在同一个应用版本下
        if (UPDATE.equals(appServiceDeployVO.getCommandType())) {
            checkInstanceConsistent(appServiceDeployVO.getInstanceId(), marketServiceDeployObjectVO.getMarketServiceId());
        }

        //7.初始化ApplicationInstanceDTO,DevopsEnvCommandDTO,DevopsEnvCommandValueDTO
        appServiceDeployVO.setMarketAppServiceId(marketServiceDeployObjectVO.getMarketServiceId());
        AppServiceInstanceDTO appServiceInstanceDTO = initMarketInstanceDTO(appServiceDeployVO);
        DevopsEnvCommandDTO devopsEnvCommandDTO = initMarketInstanceDevopsEnvCommandDTO(appServiceDeployVO);
        DevopsEnvCommandValueDTO devopsEnvCommandValueDTO = initDevopsEnvCommandValueDTO(appServiceDeployVO.getValues());

        //8.获取市场部署实例时授权secret的code ,只同步了chart的市场应用不再创建 secret
        String secretCode = null;
        if (isMarket(appServiceDeployVO.getSource()) && !Objects.isNull(marketServiceDeployObjectVO.getHarborConfigId())) {
            secretCode = makeMarketSecret(projectId, devopsEnvironmentDTO, marketServiceDeployObjectVO);
        }

        appServiceDeployVO.setNotChanged(false);
        //9.初始化自定义实例名
        String code;
        if (CREATE.equals(appServiceDeployVO.getCommandType())) {
            code = getCreateInstanceCode(appServiceDeployVO, marketServiceDeployObjectVO);
        } else {
            code = getUpdateInstanceCode(appServiceDeployVO, devopsEnvironmentDTO, appServiceInstanceDTO);
        }

        String source = "";
        //10.更新时候，如果isNotChange的值为true，则直接return,否则走操作gitops库文件逻辑
        DevopsDeployAppCenterEnvDTO devopsDeployAppCenterEnvDTO;
        if (!appServiceDeployVO.getNotChanged()) {
            //存储数据
            // 创建关联关系

            appServiceInstanceDTO.setApplicationType(appServiceDeployVO.getApplicationType());
            if (StringUtils.equalsIgnoreCase(appServiceInstanceDTO.getApplicationType(), AppSourceType.HZERO.getValue())) {
                source = AppSourceType.HZERO.getValue();
            } else {
                source = appServiceDeployVO.getSource();
            }

            devopsEnvApplicationService.createEnvAppRelationShipIfNon(appServiceDeployVO.getMarketAppServiceId(), appServiceDeployVO.getEnvironmentId(), source, appServiceCode, appServiceName);
            if (appServiceDeployVO.getCommandType().equals(CREATE)) {
                appServiceInstanceDTO.setCode(code);
                appServiceInstanceDTO.setId(baseCreate(appServiceInstanceDTO).getId());
            }
            devopsEnvCommandDTO.setObjectId(appServiceInstanceDTO.getId());
            devopsEnvCommandDTO.setValueId(devopsEnvCommandValueService.baseCreate(devopsEnvCommandValueDTO).getId());
            devopsEnvCommandDTO = devopsEnvCommandService.baseCreate(devopsEnvCommandDTO);
            appServiceInstanceDTO.setCommandId(devopsEnvCommandDTO.getId());
            baseUpdate(appServiceInstanceDTO);

            // 兼容中间件
            String chartVersion;
            chartVersion = getChartVersion(appServiceDeployVO, marketServiceDeployObjectVO);

            // 创建应用中心的应用
            if (appServiceDeployVO.getCommandType().equals(CREATE)) {
                devopsDeployAppCenterEnvDTO = devopsDeployAppCenterService.baseCreate(appServiceDeployVO.getAppName(), appServiceDeployVO.getAppCode(), projectId, appServiceInstanceDTO.getId(), appServiceDeployVO.getEnvironmentId(), appServiceDeployVO.getOperationType(), source, RdupmTypeEnum.CHART.value());
            } else {
                devopsDeployAppCenterEnvDTO = devopsDeployAppCenterService.queryByEnvIdAndCode(appServiceDeployVO.getEnvironmentId(), code);
                devopsDeployAppCenterEnvDTO.setName(appServiceDeployVO.getAppName());
                devopsDeployAppCenterService.baseUpdate(devopsDeployAppCenterEnvDTO);
            }

            // 插入部署记录
            if (Boolean.TRUE.equals(saveRecord)) {
                saveDeployRecord(marketServiceVO, appServiceInstanceDTO, devopsEnvironmentDTO, devopsEnvCommandDTO.getId(), chartVersion, appServiceDeployVO.getAppName(), appServiceDeployVO.getAppCode(), devopsDeployAppCenterEnvDTO.getId());
            }
            //如果是市场部署将部署人员添加为应用的订阅人员
            marketServiceClientOperator.subscribeApplication(marketServiceDeployObjectVO.getMarketAppId(), DetailsHelper.getUserDetails().getUserId());


            appServiceDeployVO.setInstanceId(appServiceInstanceDTO.getId());
            appServiceDeployVO.setInstanceName(code);
            if (appServiceDeployVO.getDevopsServiceReqVO() != null) {
                appServiceDeployVO.getDevopsServiceReqVO().setDevopsIngressVO(appServiceDeployVO.getDevopsIngressVO());
            }
            appServiceDeployVO.setInstanceId(appServiceInstanceDTO.getId());

            // 初始化payLoad的数据
            MarketInstanceSagaPayload instanceSagaPayload = initMarketInstanceSagaPayload(appServiceDeployVO, devopsEnvironmentDTO, userAttrDTO, marketServiceDeployObjectVO, appServiceInstanceDTO, secretCode);
            // 发送asgard
            sendCreateOrUpdateInstanceSaga(devopsEnvironmentDTO, instanceSagaPayload);
        } else {
            // 创建应用中心的应用
            if (appServiceDeployVO.getCommandType().equals(CREATE)) {
                devopsDeployAppCenterEnvDTO = devopsDeployAppCenterService.baseCreate(appServiceDeployVO.getAppName(), appServiceDeployVO.getAppCode(), projectId, appServiceInstanceDTO.getId(), appServiceDeployVO.getEnvironmentId(), appServiceDeployVO.getOperationType(), source, RdupmTypeEnum.CHART.value());
            } else {
                devopsDeployAppCenterEnvDTO = devopsDeployAppCenterService.queryByEnvIdAndCode(appServiceDeployVO.getEnvironmentId(), code);
                devopsDeployAppCenterEnvDTO.setName(appServiceDeployVO.getAppName());
                devopsDeployAppCenterService.baseUpdate(devopsDeployAppCenterEnvDTO);
            }
        }

        AppServiceInstanceVO instanceVO = ConvertUtils.convertObject(appServiceInstanceDTO, AppServiceInstanceVO.class);
        instanceVO.setAppId(devopsDeployAppCenterEnvDTO.getId());
        return instanceVO;
    }

    private String getChartVersion(MarketInstanceCreationRequestVO appServiceDeployVO, MarketServiceDeployObjectVO
            marketServiceDeployObjectVO) {
        String chartVersion;
        if (isMiddleware(appServiceDeployVO.getSource())) {
            chartVersion = marketServiceDeployObjectVO.getMarketServiceVersion();
            marketServiceDeployObjectVO.setDevopsAppServiceVersion(marketServiceDeployObjectVO.getMarketServiceVersion());
            marketServiceDeployObjectVO.setMarketChartRepository(String.format(MIDDLEWARE_CHART_REPO_TEMPLATE, gateway));
        } else if (isMarket(appServiceDeployVO.getSource()) && StringUtils.equalsIgnoreCase(appServiceDeployVO.getApplicationType(), AppSourceType.HZERO.getValue())) {
            chartVersion = marketServiceDeployObjectVO.getMarketServiceVersion();
        } else {
            chartVersion = marketServiceDeployObjectVO.getDevopsAppServiceVersion();
        }
        return chartVersion;
    }

    private void sendCreateOrUpdateInstanceSaga(DevopsEnvironmentDTO
                                                        devopsEnvironmentDTO, MarketInstanceSagaPayload instanceSagaPayload) {
        producer.apply(
                StartSagaBuilder
                        .newBuilder()
                        .withLevel(ResourceLevel.PROJECT)
                        .withSourceId(devopsEnvironmentDTO.getProjectId())
                        .withRefType("env")
                        .withSagaCode(SagaTopicCodeConstants.DEVOPS_CREATE_MARKET_INSTANCE),
                builder -> builder
                        .withPayloadAndSerialize(instanceSagaPayload)
                        .withRefId(devopsEnvironmentDTO.getId().toString()));
    }

    private MarketInstanceSagaPayload initMarketInstanceSagaPayload(MarketInstanceCreationRequestVO
                                                                            appServiceDeployVO, DevopsEnvironmentDTO devopsEnvironmentDTO, UserAttrDTO
                                                                            userAttrDTO, MarketServiceDeployObjectVO marketServiceDeployObjectVO, AppServiceInstanceDTO
                                                                            appServiceInstanceDTO, String secretCode) {
        MarketInstanceSagaPayload instanceSagaPayload = new MarketInstanceSagaPayload(devopsEnvironmentDTO.getProjectId(), userAttrDTO.getGitlabUserId(), secretCode, appServiceInstanceDTO.getCommandId());
        instanceSagaPayload.setMarketServiceDeployObjectVO(marketServiceDeployObjectVO);
        instanceSagaPayload.setMarketInstanceCreationRequestVO(appServiceDeployVO);
        instanceSagaPayload.setDevopsEnvironmentDTO(devopsEnvironmentDTO);
        instanceSagaPayload.setDevopsIngressVO(appServiceDeployVO.getDevopsIngressVO());
        instanceSagaPayload.setDevopsServiceReqVO(appServiceDeployVO.getDevopsServiceReqVO());
        return instanceSagaPayload;
    }

    private String getUpdateInstanceCode(MarketInstanceCreationRequestVO appServiceDeployVO, DevopsEnvironmentDTO
            devopsEnvironmentDTO, AppServiceInstanceDTO appServiceInstanceDTO) {
        String code;
        code = appServiceInstanceDTO.getCode();
        //更新实例的时候校验gitops库文件是否存在,处理部署实例时，由于没有创gitops文件导致的部署失败
        resourceFileCheckHandler.check(devopsEnvironmentDTO, appServiceDeployVO.getInstanceId(), code, C7NHELM_RELEASE);

        //从未关联部署配置到关联部署配置，或者从一个部署配置关联另外一个部署配置，如果values是一样的，虽然getIsNotChange为false,但是此时也应该直接设置为isNotChange为true
        AppServiceInstanceDTO oldAppServiceInstanceDTO = baseQuery(appServiceDeployVO.getInstanceId());
        String deployValue = baseQueryValueByInstanceId(appServiceInstanceDTO.getId());
        // 用上次的chart版本及values和这次的chart版本及values进行对比, 设置isNotChanged
        if (appServiceDeployVO.getMarketDeployObjectId().equals(oldAppServiceInstanceDTO.getAppServiceVersionId()) && deployValue.equals(appServiceDeployVO.getValues())) {
            appServiceDeployVO.setNotChanged(true);
        }
        return code;
    }

    private String getCreateInstanceCode(MarketInstanceCreationRequestVO
                                                 appServiceDeployVO, MarketServiceDeployObjectVO marketServiceDeployObjectVO) {
        String code;
        if (appServiceDeployVO.getInstanceName() == null || appServiceDeployVO.getInstanceName().trim().equals("")) {
            code = String.format(INSTANCE_NAME_TEMPLATE, marketServiceDeployObjectVO.getDevopsAppServiceCode(), GenerateUUID.generateUUID().substring(0, 5));
        } else {
            checkNameInternal(appServiceDeployVO.getInstanceName(), appServiceDeployVO.getEnvironmentId(), false);
            code = appServiceDeployVO.getInstanceName();
        }
        return code;
    }

    private void checkInstanceConsistent(Long instanceId, Long marketServiceId) {
        AppServiceInstanceDTO oldInstance = appServiceInstanceMapper.selectByPrimaryKey(Objects.requireNonNull(instanceId));
        CommonExAssertUtil.assertNotNull(oldInstance, "error.instance.id.not.exist");
        // 校验前后的版本属于同一个服务在同一个应用版本下, 只支持变更同个应用版本下的市场服务的修复版本
        // 而在同一个市场应用版本下的市场应用服务是同一个id，不同市场应用版本下即使是同一个市场服务名称，id也不一致
        CommonExAssertUtil.assertTrue(oldInstance.getAppServiceId().equals(marketServiceId), "error.app.version.invalid");
    }

    private MarketServiceDeployObjectVO getMarketServiceDeployObjectVO(Long projectId, Long marketDeployObjectId) {
        MarketServiceDeployObjectVO marketServiceDeployObjectVO = marketServiceClientOperator.queryDeployObject(projectId, marketDeployObjectId);
        CommonExAssertUtil.assertNotNull(marketServiceDeployObjectVO, "error.version.id.not.exist", marketDeployObjectId);
        return marketServiceDeployObjectVO;
    }

    private MarketServiceVO getMarketServiceVO(Long projectId, Long marketAppServiceId, Long marketDeployObjectId) {
        MarketServiceVO marketServiceVO = marketServiceClientOperator.queryMarketService(projectId, marketAppServiceId);
        if (marketServiceVO == null) {
            marketServiceVO = new MarketServiceVO();
            marketServiceVO.setMarketServiceCode(APP_SHELVES_CODE);
            marketServiceVO.setMarketServiceName(APP_SHELVES_NAME);
        }
        marketServiceVO.setMarketDeployObjectId(marketDeployObjectId);
        return marketServiceVO;
    }

    private DevopsEnvironmentDTO getProjectEnvironment(Long projectId, Long envId) {
        // 查询环境
        DevopsEnvironmentDTO devopsEnvironmentDTO = devopsEnvironmentService.baseQueryById(envId);
        CommonExAssertUtil.assertNotNull(devopsEnvironmentDTO, "error.env.id.not.exist", envId);
        // 校验环境和项目匹配
        CommonExAssertUtil.assertTrue(projectId.equals(devopsEnvironmentDTO.getProjectId()), MiscConstants.ERROR_OPERATING_RESOURCE_IN_OTHER_PROJECT);
        return devopsEnvironmentDTO;
    }

    private void saveDeployRecord(MarketServiceVO marketServiceVO,
                                  AppServiceInstanceDTO appServiceInstanceDTO,
                                  DevopsEnvironmentDTO devopsEnvironmentDTO,
                                  Long commandId,
                                  String chartVersion,
                                  String appName,
                                  String appCode,
                                  Long appId) {
        String deploySourceType;
        DeployType deployType;
        if (isMiddleware(appServiceInstanceDTO.getSource())) {
            deploySourceType = AppSourceType.PLATFORM_PRESET.getValue();
            deployType = DeployType.BASE_COMPONENT;
        } else if (StringUtils.equalsIgnoreCase(appServiceInstanceDTO.getApplicationType(), AppSourceType.HZERO.getValue())) {
            deploySourceType = AppSourceType.HZERO.getValue();
            deployType = DeployType.MANUAL;
        } else {
            deploySourceType = AppSourceType.MARKET.getValue();
            deployType = DeployType.MANUAL;
        }

        DeploySourceVO deploySourceVO = new DeploySourceVO();
        deploySourceVO.setType(deploySourceType);
        deploySourceVO.setMarketAppName(marketServiceVO.getMarketAppName());
        deploySourceVO.setMarketServiceName(marketServiceVO.getMarketServiceName());
        deploySourceVO.setDeployObjectId(marketServiceVO.getMarketDeployObjectId());
        devopsDeployRecordService.saveRecord(
                devopsEnvironmentDTO.getProjectId(),
                deployType,
                commandId,
                DeployModeEnum.ENV,
                devopsEnvironmentDTO.getId(),
                devopsEnvironmentDTO.getName(),
                null,
                DeployObjectTypeEnum.CHART,
                marketServiceVO.getMarketServiceName(),
                chartVersion,
                appName,
                appCode,
                appId,
                deploySourceVO);
    }

    @Override
    public void createInstanceBySaga(InstanceSagaPayload instanceSagaPayload) {
        //更新实例的时候判断当前容器目录下是否存在环境对应的GitOps文件目录，不存在则克隆
        String filePath = null;
        if (instanceSagaPayload.getAppServiceDeployVO().getType().equals(UPDATE)) {
            filePath = clusterConnectionHandler.handDevopsEnvGitRepository(
                    instanceSagaPayload.getDevopsEnvironmentDTO(),
                    instanceSagaPayload.getProjectId(),
                    instanceSagaPayload.getDevopsEnvironmentDTO().getCode(),
                    instanceSagaPayload.getDevopsEnvironmentDTO().getId(),
                    instanceSagaPayload.getDevopsEnvironmentDTO().getEnvIdRsa(),
                    instanceSagaPayload.getDevopsEnvironmentDTO().getType(),
                    instanceSagaPayload.getDevopsEnvironmentDTO().getClusterCode());
        }


        //创建实例时，如果选择了创建网络
        DevopsServiceReqVO devopsServiceReqVO = instanceSagaPayload.getDevopsServiceReqVO();
        if (devopsServiceReqVO != null) {
            devopsServiceReqVO.setAppServiceId(instanceSagaPayload.getApplicationDTO().getId());
            devopsServiceReqVO.setEnvId(instanceSagaPayload.getDevopsEnvironmentDTO().getId());
            devopsServiceService.create(instanceSagaPayload.getDevopsEnvironmentDTO().getProjectId(), devopsServiceReqVO);
        }

        try {
            //在gitops库处理instance文件
            ResourceConvertToYamlHandler<C7nHelmRelease> resourceConvertToYamlHandler = new ResourceConvertToYamlHandler<>();
            resourceConvertToYamlHandler.setType(getC7NHelmRelease(
                    instanceSagaPayload.getAppServiceDeployVO().getInstanceName(),
                    instanceSagaPayload.getAppServiceVersionDTO().getRepository(),
                    instanceSagaPayload.getApplicationDTO().getId(),
                    instanceSagaPayload.getCommandId(),
                    instanceSagaPayload.getApplicationDTO().getCode(),
                    instanceSagaPayload.getAppServiceVersionDTO().getVersion(),
                    instanceSagaPayload.getAppServiceDeployVO().getValues(),
                    instanceSagaPayload.getAppServiceDeployVO().getAppServiceVersionId(),
                    instanceSagaPayload.getSecretCode(),
                    instanceSagaPayload.getDevopsEnvironmentDTO()));

            resourceConvertToYamlHandler.operationEnvGitlabFile(
                    RELEASE_PREFIX + instanceSagaPayload.getAppServiceDeployVO().getInstanceName(),
                    instanceSagaPayload.getDevopsEnvironmentDTO().getGitlabEnvProjectId().intValue(),
                    instanceSagaPayload.getAppServiceDeployVO().getType(),
                    instanceSagaPayload.getGitlabUserId(),
                    instanceSagaPayload.getAppServiceDeployVO().getInstanceId(), C7NHELM_RELEASE, null, false, instanceSagaPayload.getDevopsEnvironmentDTO().getId(), filePath);

            //创建实例成功 发送web hook json
            if (CREATE.equals(instanceSagaPayload.getAppServiceDeployVO().getType())) {
                AppServiceInstanceDTO appServiceInstanceDTO = appServiceInstanceMapper.selectByPrimaryKey(instanceSagaPayload.getAppServiceDeployVO().getInstanceId());
                appServiceInstanceDTO.setProjectId(instanceSagaPayload.getProjectId());
                DevopsEnvCommandDTO devopsEnvCommandDTO = devopsEnvCommandService
                        .baseQueryByObject(ObjectType.INSTANCE.getType(), appServiceInstanceDTO.getId());
                sendNotificationService.sendInstanceStatusUpdate(appServiceInstanceDTO, devopsEnvCommandDTO, InstanceStatus.RUNNING.getStatus());
            }
        } catch (Exception e) {
            //有异常更新实例以及command的状态
            AppServiceInstanceDTO appServiceInstanceDTO = baseQuery(instanceSagaPayload.getAppServiceDeployVO().getInstanceId());
            DevopsEnvFileResourceDTO devopsEnvFileResourceDTO = devopsEnvFileResourceService
                    .baseQueryByEnvIdAndResourceId(instanceSagaPayload.getDevopsEnvironmentDTO().getId(), appServiceInstanceDTO.getId(), HELM_RELEASE);
            filePath = devopsEnvFileResourceDTO == null ? RELEASE_PREFIX + appServiceInstanceDTO.getCode() + YAML_SUFFIX : devopsEnvFileResourceDTO.getFilePath();
            // 这里只考虑了创建失败的情况，这说明是gitlab超时
            if (!CREATE.equals(instanceSagaPayload.getAppServiceDeployVO().getType()) || !gitlabServiceClientOperator.getFile(TypeUtil.objToInteger(instanceSagaPayload.getDevopsEnvironmentDTO().getGitlabEnvProjectId()), MASTER,
                    filePath)) {
                throw e;
            }
            if (CREATE.equals(instanceSagaPayload.getAppServiceDeployVO().getType())) {
                //创建实例资源失败，发送webhook json
                DevopsEnvCommandDTO devopsEnvCommandDTO = devopsEnvCommandService
                        .baseQueryByObject(ObjectType.INSTANCE.getType(), appServiceInstanceDTO.getId());
                sendNotificationService.sendInstanceStatusUpdate(appServiceInstanceDTO, devopsEnvCommandDTO, InstanceStatus.FAILED.getStatus());
            }
            // 更新的超时情况暂未处理
        }
    }

    @Override
    public void createMarketInstanceBySaga(MarketInstanceSagaPayload instanceSagaPayload) {
        //更新实例的时候判断当前容器目录下是否存在环境对应的GitOps文件目录，不存在则克隆
        String filePath = null;
        if (instanceSagaPayload.getMarketInstanceCreationRequestVO().getCommandType().equals(UPDATE)) {
            filePath = clusterConnectionHandler.handDevopsEnvGitRepository(
                    instanceSagaPayload.getDevopsEnvironmentDTO(),
                    instanceSagaPayload.getProjectId(),
                    instanceSagaPayload.getDevopsEnvironmentDTO().getCode(),
                    instanceSagaPayload.getDevopsEnvironmentDTO().getId(),
                    instanceSagaPayload.getDevopsEnvironmentDTO().getEnvIdRsa(),
                    instanceSagaPayload.getDevopsEnvironmentDTO().getType(),
                    instanceSagaPayload.getDevopsEnvironmentDTO().getClusterCode());
        }


        //创建实例时，如果选择了创建网络
        if (instanceSagaPayload.getDevopsServiceReqVO() != null) {
            instanceSagaPayload.getDevopsServiceReqVO().setAppServiceId(instanceSagaPayload.getMarketServiceDeployObjectVO().getMarketServiceId());
            devopsServiceService.create(instanceSagaPayload.getDevopsEnvironmentDTO().getProjectId(), instanceSagaPayload.getDevopsServiceReqVO());
        }

        try {
            //在gitops库处理instance文件
            ResourceConvertToYamlHandler<C7nHelmRelease> resourceConvertToYamlHandler = new ResourceConvertToYamlHandler<>();
            resourceConvertToYamlHandler.setType(getC7NHelmReleaseForMarketServiceInstance(
                    instanceSagaPayload.getMarketInstanceCreationRequestVO().getInstanceName(),
                    instanceSagaPayload.getMarketServiceDeployObjectVO().getMarketChartRepository(),
                    instanceSagaPayload.getMarketServiceDeployObjectVO().getMarketServiceId(),
                    instanceSagaPayload.getCommandId(),
                    instanceSagaPayload.getMarketServiceDeployObjectVO().getMarketArtifactCode(),
                    instanceSagaPayload.getMarketServiceDeployObjectVO().getDevopsAppServiceVersion(),
                    instanceSagaPayload.getMarketInstanceCreationRequestVO().getValues(),
                    instanceSagaPayload.getMarketInstanceCreationRequestVO().getMarketDeployObjectId(),
                    instanceSagaPayload.getSecretCode(),
                    instanceSagaPayload.getMarketInstanceCreationRequestVO().getSource(),
                    instanceSagaPayload.getDevopsEnvironmentDTO()));

            resourceConvertToYamlHandler.operationEnvGitlabFile(
                    RELEASE_PREFIX + instanceSagaPayload.getMarketInstanceCreationRequestVO().getInstanceName(),
                    instanceSagaPayload.getDevopsEnvironmentDTO().getGitlabEnvProjectId().intValue(),
                    instanceSagaPayload.getMarketInstanceCreationRequestVO().getCommandType(),
                    instanceSagaPayload.getGitlabUserId(),
                    instanceSagaPayload.getMarketInstanceCreationRequestVO().getInstanceId(), C7NHELM_RELEASE, null, false, instanceSagaPayload.getDevopsEnvironmentDTO().getId(), filePath);

            //创建实例成功 发送web hook json
            if (CREATE.equals(instanceSagaPayload.getMarketInstanceCreationRequestVO().getCommandType())) {
                AppServiceInstanceDTO appServiceInstanceDTO = appServiceInstanceMapper.selectByPrimaryKey(instanceSagaPayload.getMarketInstanceCreationRequestVO().getInstanceId());
                appServiceInstanceDTO.setProjectId(instanceSagaPayload.getProjectId());
                DevopsEnvCommandDTO devopsEnvCommandDTO = devopsEnvCommandService
                        .baseQueryByObject(ObjectType.INSTANCE.getType(), appServiceInstanceDTO.getId());
                sendNotificationService.sendInstanceStatusUpdate(appServiceInstanceDTO, devopsEnvCommandDTO, InstanceStatus.RUNNING.getStatus());
            }
        } catch (Exception e) {
            //有异常更新实例以及command的状态
            AppServiceInstanceDTO appServiceInstanceDTO = baseQuery(instanceSagaPayload.getMarketInstanceCreationRequestVO().getInstanceId());
            DevopsEnvFileResourceDTO devopsEnvFileResourceDTO = devopsEnvFileResourceService
                    .baseQueryByEnvIdAndResourceId(instanceSagaPayload.getDevopsEnvironmentDTO().getId(), appServiceInstanceDTO.getId(), HELM_RELEASE);
            filePath = devopsEnvFileResourceDTO == null ? RELEASE_PREFIX + appServiceInstanceDTO.getCode() + YAML_SUFFIX : devopsEnvFileResourceDTO.getFilePath();
            // 这里只考虑了创建失败的情况，这说明是gitlab超时
            if (!CREATE.equals(instanceSagaPayload.getMarketInstanceCreationRequestVO().getCommandType()) || !gitlabServiceClientOperator.getFile(TypeUtil.objToInteger(instanceSagaPayload.getDevopsEnvironmentDTO().getGitlabEnvProjectId()), MASTER,
                    filePath)) {
                throw e;
            }
            if (CREATE.equals(instanceSagaPayload.getMarketInstanceCreationRequestVO().getCommandType())) {
                //创建实例资源失败，发送webhook json
                DevopsEnvCommandDTO devopsEnvCommandDTO = devopsEnvCommandService
                        .baseQueryByObject(ObjectType.INSTANCE.getType(), appServiceInstanceDTO.getId());
                sendNotificationService.sendInstanceStatusUpdate(appServiceInstanceDTO, devopsEnvCommandDTO, InstanceStatus.FAILED.getStatus());
            }
            // 更新的超时情况暂未处理
        }
    }

    @Override
    public AppServiceInstanceRepVO queryByCommandId(Long commandId) {
        DevopsEnvCommandDTO devopsEnvCommandDTO = devopsEnvCommandService.baseQuery(commandId);
        if (commandId == null) {
            throw new CommonException("error.command.not.exist", commandId);
        }
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
        DevopsEnvCommandValueDTO devopsEnvCommandValueDTO = initDevopsEnvCommandValueDTO(appServiceDeployVO.getValues());

        //实例相关对象数据库操作
        DevopsDeployAppCenterEnvDTO devopsDeployAppCenterEnvDTO;
        if (appServiceDeployVO.getType().equals(CREATE)) {
            appServiceInstanceDTO.setCode(appServiceDeployVO.getInstanceName());
            appServiceInstanceDTO.setId(baseCreate(appServiceInstanceDTO).getId());
            devopsDeployAppCenterEnvDTO = devopsDeployAppCenterService.baseCreate(appServiceDeployVO.getAppName(), appServiceDeployVO.getAppCode(), devopsEnvironmentDTO.getProjectId(), appServiceInstanceDTO.getId(), appServiceDeployVO.getEnvironmentId(), OperationTypeEnum.CREATE_APP.value(), appServiceInstanceDTO.getSource(), RdupmTypeEnum.CHART.value());

        } else {
            baseUpdate(appServiceInstanceDTO);
            devopsDeployAppCenterEnvDTO = devopsDeployAppCenterService.queryByRdupmTypeAndObjectId(RdupmTypeEnum.CHART, appServiceInstanceDTO.getId());
        }

        devopsEnvCommandDTO.setCreatedBy(userId);
        devopsEnvCommandDTO.setObjectId(appServiceInstanceDTO.getId());
        devopsEnvCommandDTO.setValueId(devopsEnvCommandValueService.baseCreate(devopsEnvCommandValueDTO).getId());
        devopsEnvCommandDTO = devopsEnvCommandService.baseCreate(devopsEnvCommandDTO);
        appServiceInstanceDTO.setCommandId(devopsEnvCommandDTO.getId());
        baseUpdate(appServiceInstanceDTO);

        // 插入应用服务和环境的关联关系
        AppServiceDTO appServiceDTO = applicationService.baseQuery(appServiceInstanceDTO.getAppServiceId());
        AppServiceVersionDTO appServiceVersionDTO = appServiceVersionService.baseQuery(devopsEnvCommandDTO.getObjectVersionId());
        boolean isProjectAppService = devopsEnvironmentDTO.getProjectId().equals(appServiceDTO.getProjectId());
        if (appServiceInstanceDTO.getAppServiceId() != null) {
            ApplicationCenterEnum appSourceType = isProjectAppService ? ApplicationCenterEnum.PROJECT : ApplicationCenterEnum.SHARE;
            String serviceCode = appServiceDTO.getCode();
            String serviceName = appServiceDTO.getName();
            devopsEnvApplicationService.createEnvAppRelationShipIfNon(appServiceInstanceDTO.getAppServiceId(), devopsEnvironmentDTO.getId(), appSourceType.value(), serviceCode, serviceName);
        }


        //插入部署记录
        ProjectDTO projectDTO = baseServiceClientOperator.queryIamProjectById(devopsEnvironmentDTO.getProjectId());
        devopsDeployRecordService.saveRecord(devopsEnvironmentDTO.getProjectId(),
                DeployType.MANUAL,
                devopsEnvCommandDTO.getId(),
                DeployModeEnum.ENV,
                devopsEnvironmentDTO.getId(),
                devopsEnvironmentDTO.getName(),
                null,
                DeployObjectTypeEnum.CHART,
                appServiceDTO.getName(),
                appServiceVersionDTO.getVersion(),
                appServiceDeployVO.getAppName(),
                appServiceDeployVO.getAppCode(),
                devopsDeployAppCenterEnvDTO.getId(),
                new DeploySourceVO(isProjectAppService ? AppSourceType.CURRENT_PROJECT : AppSourceType.SHARE, projectDTO.getName()));


        return ConvertUtils.convertObject(appServiceInstanceDTO, AppServiceInstanceVO.class);
    }

    @Override
    public AppServiceInstanceVO createOrUpdateMarketInstanceByGitOps(MarketInstanceCreationRequestVO
                                                                             appServiceDeployVO, Long userId) {
        DevopsEnvironmentDTO devopsEnvironmentDTO = devopsEnvironmentService.baseQueryById(appServiceDeployVO.getEnvironmentId());
        //校验环境是否连接
        clusterConnectionHandler.checkEnvConnection(devopsEnvironmentDTO.getClusterId());

        //校验values
        FileUtil.checkYamlFormat(appServiceDeployVO.getValues());

        //初始化ApplicationInstanceDTO,DevopsEnvCommandDTO,DevopsEnvCommandValueDTO
        AppServiceInstanceDTO appServiceInstanceDTO = initMarketInstanceDTO(appServiceDeployVO);
        DevopsEnvCommandDTO devopsEnvCommandDTO = initMarketInstanceDevopsEnvCommandDTO(appServiceDeployVO);
        DevopsEnvCommandValueDTO devopsEnvCommandValueDTO = initDevopsEnvCommandValueDTO(appServiceDeployVO.getValues());

        //实例相关对象数据库操作
        // 创建或跟新关联关系
        // TODO: 2021/6/30 hzero 组件的serviceCode,和serviceName
        String source = AppSourceType.MARKET.getValue();
        String serviceName = null;
        String serviceCode = null;
        MarketServiceDeployObjectVO marketServiceDeployObjectVO = marketServiceClientOperator.queryDeployObject(0L, appServiceDeployVO.getMarketDeployObjectId());
        if (!Objects.isNull(marketServiceDeployObjectVO)) {
            serviceName = marketServiceDeployObjectVO.getDevopsAppServiceName();
            serviceCode = marketServiceDeployObjectVO.getDevopsAppServiceCode();
        }
        devopsEnvApplicationService.createEnvAppRelationShipIfNon(appServiceInstanceDTO.getAppServiceId(), appServiceDeployVO.getEnvironmentId(), source, serviceCode, serviceName);
        DevopsDeployAppCenterEnvDTO devopsDeployAppCenterEnvDTO;
        if (appServiceDeployVO.getCommandType().equals(CREATE)) {
            appServiceInstanceDTO.setCode(appServiceDeployVO.getInstanceName());
            appServiceInstanceDTO.setId(baseCreate(appServiceInstanceDTO).getId());
            devopsDeployAppCenterEnvDTO = devopsDeployAppCenterService.baseCreate(appServiceDeployVO.getAppName(), appServiceDeployVO.getAppCode(), devopsEnvironmentDTO.getProjectId(), appServiceInstanceDTO.getId(), appServiceDeployVO.getEnvironmentId(), OperationTypeEnum.CREATE_APP.value(), appServiceInstanceDTO.getSource(), RdupmTypeEnum.CHART.value());

        } else {
            baseUpdate(appServiceInstanceDTO);
            devopsDeployAppCenterEnvDTO = devopsDeployAppCenterService.queryByRdupmTypeAndObjectId(RdupmTypeEnum.CHART, appServiceInstanceDTO.getId());
        }
        devopsEnvCommandDTO.setCreatedBy(userId);
        devopsEnvCommandDTO.setObjectId(appServiceInstanceDTO.getId());
        devopsEnvCommandDTO.setValueId(devopsEnvCommandValueService.baseCreate(devopsEnvCommandValueDTO).getId());
        devopsEnvCommandDTO = devopsEnvCommandService.baseCreate(devopsEnvCommandDTO);
        appServiceInstanceDTO.setCommandId(devopsEnvCommandDTO.getId());
        baseUpdate(appServiceInstanceDTO);

        // 插入部署记录
        MarketServiceVO marketServiceVO = getMarketServiceVO(devopsEnvironmentDTO.getProjectId(), appServiceDeployVO.getMarketAppServiceId(), appServiceDeployVO.getMarketDeployObjectId());
        saveDeployRecord(marketServiceVO, appServiceInstanceDTO, devopsEnvironmentDTO, devopsEnvCommandDTO.getId(), appServiceDeployVO.getChartVersion(), appServiceDeployVO.getAppName(), appServiceDeployVO.getAppCode(), devopsDeployAppCenterEnvDTO.getId());

        return ConvertUtils.convertObject(appServiceInstanceDTO, AppServiceInstanceVO.class);
    }

    @Override
    public List<RunningInstanceVO> listRunningInstance(Long projectId, Long appServiceId, Long
            appServiceVersionId, Long envId) {
        return ConvertUtils.convertList(appServiceInstanceMapper.listApplicationInstanceCode(
                projectId, envId, appServiceVersionId, appServiceId), RunningInstanceVO.class);
    }

    @Override
    public List<AppServiceInstanceVO> listMarketInstance(Long envId) {
        return appServiceInstanceMapper.listMarketInstance(envId);
    }

    @Override
    public List<RunningInstanceVO> listByAppIdAndEnvId(Long projectId, Long appServiceId, Long envId) {
        return ConvertUtils.convertList(appServiceInstanceMapper.listRunningAndFailedInstance(projectId, envId, appServiceId),
                RunningInstanceVO.class);
    }


    @Override
    public void stopInstance(Long projectId, Long instanceId) {
        handleStartOrStopInstance(projectId, instanceId, CommandType.STOP.getType());
    }

    @Override
    public void startInstance(Long projectId, Long instanceId) {
        handleStartOrStopInstance(projectId, instanceId, CommandType.RESTART.getType());
    }


    @Override
    public DevopsEnvCommandDTO restartInstance(Long projectId, Long instanceId, boolean isFromPipeline, Boolean
            saveRecord) {
        AppServiceInstanceDTO appServiceInstanceDTO = baseQuery(instanceId);
        if (AppSourceType.NORMAL.getValue().equals(appServiceInstanceDTO.getSource())) {
            return doRestartNormalInstance(projectId, appServiceInstanceDTO, isFromPipeline);
        } else {
            return doRestartMarketInstance(projectId, appServiceInstanceDTO, saveRecord);
        }
    }

    private DevopsEnvCommandDTO doRestartNormalInstance(Long projectId, AppServiceInstanceDTO appServiceInstanceDTO,
                                                        boolean isFromPipeline) {
        Long instanceId = appServiceInstanceDTO.getId();

        DevopsDeployAppCenterEnvDTO devopsDeployAppCenterEnvDTO = devopsDeployAppCenterService.queryByRdupmTypeAndObjectId(RdupmTypeEnum.CHART, appServiceInstanceDTO.getId());

        DevopsEnvironmentDTO devopsEnvironmentDTO = permissionHelper.checkEnvBelongToProject(projectId, appServiceInstanceDTO.getEnvId());

        UserAttrDTO userAttrDTO = userAttrService.baseQueryById(TypeUtil.objToLong(GitUserNameUtil.getUserId()));

        //校验环境相关信息
        devopsEnvironmentService.checkEnv(devopsEnvironmentDTO, userAttrDTO);

        DevopsEnvCommandDTO devopsEnvCommandDTO = devopsEnvCommandService.baseQuery(appServiceInstanceDTO.getCommandId());
        AppServiceDTO appServiceDTO = applicationService.baseQuery(appServiceInstanceDTO.getAppServiceId());
        AppServiceVersionDTO appServiceVersionDTO = appServiceVersionService
                .baseQuery(devopsEnvCommandDTO.getObjectVersionId());

        String value = baseQueryValueByInstanceId(instanceId);

        devopsEnvCommandDTO.setId(null);
        devopsEnvCommandDTO.setCommandType(CommandType.UPDATE.getType());
        devopsEnvCommandDTO.setStatus(CommandStatus.OPERATING.getStatus());
        devopsEnvCommandDTO.setSha(null);
        devopsEnvCommandDTO = devopsEnvCommandService.baseCreate(devopsEnvCommandDTO);

        updateInstanceStatus(instanceId, devopsEnvCommandDTO.getId(), InstanceStatus.OPERATING.getStatus());

        //获取授权secret
        String secretCode = getSecret(appServiceDTO, appServiceVersionDTO.getId(), devopsEnvironmentDTO);

        //插入部署记录
        boolean isProjectAppService = devopsEnvironmentDTO.getProjectId().equals(appServiceDTO.getProjectId());
        ProjectDTO projectDTO = baseServiceClientOperator.queryIamProjectById(devopsEnvironmentDTO.getProjectId());
        devopsDeployRecordService.saveRecord(
                devopsEnvironmentDTO.getProjectId(),
                isFromPipeline ? DeployType.AUTO : DeployType.MANUAL,
                devopsEnvCommandDTO.getId(),
                DeployModeEnum.ENV,
                devopsEnvironmentDTO.getId(),
                devopsEnvironmentDTO.getName(),
                null,
                DeployObjectTypeEnum.CHART,
                appServiceDTO.getName(),
                appServiceVersionDTO.getVersion(),
                devopsDeployAppCenterEnvDTO.getName(),
                devopsDeployAppCenterEnvDTO.getCode(),
                devopsDeployAppCenterEnvDTO.getId(),
                new DeploySourceVO(isProjectAppService ? AppSourceType.CURRENT_PROJECT : AppSourceType.SHARE, projectDTO.getName()));


        AppServiceDeployVO appServiceDeployVO = new AppServiceDeployVO();
        appServiceDeployVO.setInstanceId(appServiceInstanceDTO.getId());
        appServiceDeployVO.setValues(value);
        appServiceDeployVO.setType(UPDATE);
        appServiceDeployVO.setAppServiceVersionId(appServiceVersionDTO.getId());
        appServiceDeployVO.setInstanceName(appServiceInstanceDTO.getCode());
        InstanceSagaPayload instanceSagaPayload = new InstanceSagaPayload(devopsEnvironmentDTO.getProjectId(), userAttrDTO.getGitlabUserId(), secretCode, devopsEnvCommandDTO.getId());
        instanceSagaPayload.setApplicationDTO(appServiceDTO);
        instanceSagaPayload.setAppServiceVersionDTO(appServiceVersionDTO);
        instanceSagaPayload.setAppServiceDeployVO(appServiceDeployVO);
        instanceSagaPayload.setDevopsEnvironmentDTO(devopsEnvironmentDTO);

        //目前重新部署也走gitops逻辑
        producer.apply(
                StartSagaBuilder
                        .newBuilder()
                        .withLevel(ResourceLevel.PROJECT)
                        .withSourceId(devopsEnvironmentDTO.getProjectId())
                        .withRefType("env")
                        .withSagaCode(SagaTopicCodeConstants.DEVOPS_CREATE_INSTANCE),
                builder -> builder
                        .withPayloadAndSerialize(instanceSagaPayload)
                        .withRefId(devopsEnvironmentDTO.getId().toString()));

        return devopsEnvCommandDTO;

    }

    private DevopsEnvCommandDTO doRestartMarketInstance(Long projectId, AppServiceInstanceDTO
            appServiceInstanceDTO, Boolean saveRecord) {
        // 查询市场应用服务, 确认存在
        Long instanceId = appServiceInstanceDTO.getId();
        DevopsDeployAppCenterEnvDTO devopsDeployAppCenterEnvDTO = devopsDeployAppCenterService.queryByRdupmTypeAndObjectId(RdupmTypeEnum.CHART, instanceId);
        DevopsEnvironmentDTO devopsEnvironmentDTO = permissionHelper.checkEnvBelongToProject(projectId, appServiceInstanceDTO.getEnvId());

        UserAttrDTO userAttrDTO = userAttrService.baseQueryById(TypeUtil.objToLong(GitUserNameUtil.getUserId()));

        //校验环境相关信息
        devopsEnvironmentService.checkEnv(devopsEnvironmentDTO, userAttrDTO);

        DevopsEnvCommandDTO devopsEnvCommandDTO = devopsEnvCommandService.baseQuery(appServiceInstanceDTO.getCommandId());
        MarketServiceDeployObjectVO appServiceVersion = marketServiceClientOperator.queryDeployObject(projectId, devopsEnvCommandDTO.getObjectVersionId());
        MarketServiceVO marketServiceVO = getMarketServiceVO(projectId, appServiceVersion.getMarketServiceId(), appServiceInstanceDTO.getAppServiceVersionId());

        String value = baseQueryValueByInstanceId(instanceId);

        devopsEnvCommandDTO.setId(null);
        devopsEnvCommandDTO.setCommandType(CommandType.UPDATE.getType());
        devopsEnvCommandDTO.setStatus(CommandStatus.OPERATING.getStatus());
        devopsEnvCommandDTO = devopsEnvCommandService.baseCreate(devopsEnvCommandDTO);

        updateInstanceStatus(instanceId, devopsEnvCommandDTO.getId(), InstanceStatus.OPERATING.getStatus());

        //获取授权secret
        String secretCode = null;
        if (isMarket(appServiceInstanceDTO.getSource()) && !Objects.isNull(appServiceVersion.getHarborConfigId())) {
            secretCode = makeMarketSecret(projectId, devopsEnvironmentDTO, appServiceVersion);
        }

        MarketInstanceCreationRequestVO appServiceDeployVO = new MarketInstanceCreationRequestVO();
        // 兼容中间件
        String chartVersion;
        if (isMiddleware(appServiceInstanceDTO.getSource())) {
            chartVersion = appServiceVersion.getMarketServiceVersion();
            appServiceVersion.setDevopsAppServiceVersion(appServiceVersion.getMarketServiceVersion());
            appServiceVersion.setMarketChartRepository(String.format(MIDDLEWARE_CHART_REPO_TEMPLATE, gateway));
            appServiceDeployVO.setSource(AppSourceType.MIDDLEWARE.getValue());
        } else {
            chartVersion = appServiceVersion.getDevopsAppServiceVersion();
            appServiceDeployVO.setSource(AppSourceType.MARKET.getValue());
        }
        //插入部署记录
        if (Boolean.TRUE.equals(saveRecord)) {
            saveDeployRecord(marketServiceVO, appServiceInstanceDTO, devopsEnvironmentDTO, devopsEnvCommandDTO.getId(), chartVersion, appServiceDeployVO.getAppName(), appServiceDeployVO.getAppCode(), devopsDeployAppCenterEnvDTO.getId());
        }

        appServiceDeployVO.setInstanceId(appServiceInstanceDTO.getId());
        appServiceDeployVO.setInstanceName(appServiceInstanceDTO.getCode());
        appServiceDeployVO.setValues(value);
        appServiceDeployVO.setMarketDeployObjectId(appServiceVersion.getId());
        appServiceDeployVO.setCommandType(CommandType.UPDATE.getType());
        MarketInstanceSagaPayload instanceSagaPayload = new MarketInstanceSagaPayload(devopsEnvironmentDTO.getProjectId(), userAttrDTO.getGitlabUserId(), secretCode, appServiceInstanceDTO.getCommandId());
        instanceSagaPayload.setMarketServiceDeployObjectVO(appServiceVersion);
        instanceSagaPayload.setMarketInstanceCreationRequestVO(appServiceDeployVO);
        instanceSagaPayload.setDevopsEnvironmentDTO(devopsEnvironmentDTO);
        instanceSagaPayload.setCommandId(devopsEnvCommandDTO.getId());


        producer.apply(
                StartSagaBuilder
                        .newBuilder()
                        .withLevel(ResourceLevel.PROJECT)
                        .withSourceId(devopsEnvironmentDTO.getProjectId())
                        .withRefType("env")
                        .withSagaCode(SagaTopicCodeConstants.DEVOPS_CREATE_MARKET_INSTANCE),
                builder -> builder
                        .withPayloadAndSerialize(instanceSagaPayload)
                        .withRefId(devopsEnvironmentDTO.getId().toString()));

        return devopsEnvCommandDTO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteInstance(Long projectId, Long instanceId, Boolean deletePrometheus) {
        AppServiceInstanceDTO appServiceInstanceDTO = baseQuery(instanceId);

        if (appServiceInstanceDTO == null) {
            return;
        }

        devopsDeployAppCenterService.checkEnableDeleteAndThrowE(projectId, RdupmTypeEnum.CHART, appServiceInstanceDTO.getId());

        // 加锁
        if (Boolean.FALSE.equals(stringRedisTemplate.opsForValue().setIfAbsent(String.format(APP_INSTANCE_DELETE_REDIS_KEY, instanceId), "lock", 5, TimeUnit.MINUTES))) {
            throw new CommonException(ERROR_APP_INSTANCE_IS_OPERATING);
        }


        DevopsEnvironmentDTO devopsEnvironmentDTO = devopsEnvironmentService.baseQueryById(appServiceInstanceDTO.getEnvId());

        // 内部调用不需要校验
        if (projectId != null) {
            CommonExAssertUtil.assertTrue(projectId.equals(devopsEnvironmentDTO.getProjectId()), MiscConstants.ERROR_OPERATING_RESOURCE_IN_OTHER_PROJECT);
        }

        UserAttrDTO userAttrDTO = userAttrService.baseQueryById(TypeUtil.objToLong(GitUserNameUtil.getUserId()));

        // 校验环境相关信息
        devopsEnvironmentService.checkEnv(devopsEnvironmentDTO, userAttrDTO);

        DevopsEnvCommandDTO devopsEnvCommandDTO = devopsEnvCommandService.baseQuery(appServiceInstanceDTO.getCommandId());
        devopsEnvCommandDTO.setCommandType(CommandType.DELETE.getType());
        devopsEnvCommandDTO.setStatus(CommandStatus.OPERATING.getStatus());
        devopsEnvCommandDTO.setId(null);
        devopsEnvCommandDTO = devopsEnvCommandService.baseCreate(devopsEnvCommandDTO);

        updateInstanceStatus(instanceId, devopsEnvCommandDTO.getId(), InstanceStatus.OPERATING.getStatus());


        //判断当前容器目录下是否存在环境对应的gitops文件目录，不存在则克隆
        String path = clusterConnectionHandler.handDevopsEnvGitRepository(
                devopsEnvironmentDTO,
                devopsEnvironmentDTO.getProjectId(),
                devopsEnvironmentDTO.getCode(),
                devopsEnvironmentDTO.getId(),
                devopsEnvironmentDTO.getEnvIdRsa(),
                devopsEnvironmentDTO.getType(),
                devopsEnvironmentDTO.getClusterCode());

        DevopsEnvFileResourceDTO devopsEnvFileResourceDTO = devopsEnvFileResourceService
                .baseQueryByEnvIdAndResourceId(devopsEnvironmentDTO.getId(), instanceId, C7NHELM_RELEASE);
        //如果文件对象对应关系不存在，证明没有部署成功，删掉gitops文件,删掉资源
        if (devopsEnvFileResourceDTO == null) {
            appServiceInstanceMapper.deleteByPrimaryKey(instanceId);
            devopsDeployRecordService.deleteRelatedRecordOfInstance(instanceId);
            // 删除相关数据
            devopsEnvCommandService.cascadeDeleteByInstanceId(instanceId);
            devopsDeployAppCenterService.deleteByEnvIdAndObjectIdAndRdupmType(devopsEnvironmentDTO.getId(), appServiceInstanceDTO.getId(), RdupmTypeEnum.CHART.value());
            if (gitlabServiceClientOperator.getFile(TypeUtil.objToInteger(devopsEnvironmentDTO.getGitlabEnvProjectId()), MASTER,
                    RELEASE_PREFIX + appServiceInstanceDTO.getCode() + YAML_SUFFIX)) {
                gitlabServiceClientOperator.deleteFile(
                        TypeUtil.objToInteger(devopsEnvironmentDTO.getGitlabEnvProjectId()),
                        RELEASE_PREFIX + appServiceInstanceDTO.getCode() + YAML_SUFFIX,
                        "DELETE FILE",
                        TypeUtil.objToInteger(userAttrDTO.getGitlabUserId()), "master");
            }
            return;
        } else {
            //如果文件对象对应关系存在，但是gitops文件不存在，也直接删掉资源
            if (!gitlabServiceClientOperator.getFile(TypeUtil.objToInteger(devopsEnvironmentDTO.getGitlabEnvProjectId()), MASTER,
                    devopsEnvFileResourceDTO.getFilePath())) {
                appServiceInstanceMapper.deleteByPrimaryKey(instanceId);
                devopsDeployRecordService.deleteRelatedRecordOfInstance(instanceId);
                devopsEnvCommandService.cascadeDeleteByInstanceId(instanceId);
                devopsDeployAppCenterService.deleteByEnvIdAndObjectIdAndRdupmType(devopsEnvironmentDTO.getId(), appServiceInstanceDTO.getId(), RdupmTypeEnum.CHART.value());
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
                        TypeUtil.objToInteger(userAttrDTO.getGitlabUserId()), "master");
            }
        } else {
            ResourceConvertToYamlHandler<C7nHelmRelease> resourceConvertToYamlHandler = new ResourceConvertToYamlHandler<>();
            C7nHelmRelease c7nHelmRelease = new C7nHelmRelease();
            Metadata metadata = new Metadata();
            metadata.setName(appServiceInstanceDTO.getCode());
            c7nHelmRelease.setMetadata(metadata);
            resourceConvertToYamlHandler.setType(c7nHelmRelease);
            Integer gitlabProjectId = TypeUtil.objToInteger(devopsEnvironmentDTO.getGitlabEnvProjectId());
            resourceConvertToYamlHandler.operationEnvGitlabFile(
                    RELEASE_PREFIX + appServiceInstanceDTO.getCode(),
                    gitlabProjectId,
                    "delete",
                    userAttrDTO.getGitlabUserId(),
                    appServiceInstanceDTO.getId(), C7NHELM_RELEASE, null, false, devopsEnvironmentDTO.getId(), path);
        }
        // 删除锁
        stringRedisTemplate.delete(String.format(APP_INSTANCE_DELETE_REDIS_KEY, instanceId));
        //删除实例发送web hook josn通知
        sendNotificationService.sendWhenInstanceSuccessOrDelete(appServiceInstanceDTO, SendSettingEnum.DELETE_RESOURCE.value());
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

        devopsDeployRecordService.deleteRelatedRecordOfInstance(instanceId);
        devopsEnvCommandService.cascadeDeleteByInstanceId(instanceId);
        appServiceInstanceMapper.deleteByPrimaryKey(instanceId);
        devopsDeployAppCenterService.deleteByEnvIdAndObjectIdAndRdupmType(devopsEnvironmentDTO.getId(), instanceId, RdupmTypeEnum.CHART.value());

        // 删除prometheus的相关信息
        if ("prometheus-operator".equals(instanceDTO.getComponentChartName())) {
            Long clusterId = devopsClusterMapper.queryClusterIdBySystemEnvId(instanceDTO.getEnvId());
            // 删除devopsClusterResource
            DevopsClusterResourceDTO devopsClusterResourceDTO = new DevopsClusterResourceDTO();
            devopsClusterResourceDTO.setClusterId(clusterId);
            devopsClusterResourceDTO.setType("prometheus");
            devopsClusterResourceMapper.delete(devopsClusterResourceDTO);

            // 删除prometheus配置信息
            DevopsPrometheusDTO devopsPrometheusDTO = new DevopsPrometheusDTO();
            devopsPrometheusDTO.setClusterId(clusterId);
            devopsPrometheusMapper.delete(devopsPrometheusDTO);
        }
    }


    @Override
    public void checkName(String code, Long envId) {
        checkNameInternal(code, envId, false);
    }

    @Override
    public boolean isNameValid(String code, Long envId) {
        // 这里校验集群下code唯一而不是环境下code唯一是因为helm的release是需要集群下唯一的
        return AppServiceInstanceValidator.isCodeValid(code)
                && !appServiceInstanceMapper.checkCodeExist(code, envId);
    }

    /**
     * 校验实例名称格式是否符合，且是否重名
     *
     * @param code           实例code
     * @param envId          环境id
     * @param isFromPipeline 是否从流水自动部署中进行校验，如果是，不再校验流水线中的将要创建的实例名称是否存在
     */
    private void checkNameInternal(String code, Long envId, boolean isFromPipeline) {
        AppServiceInstanceValidator.checkCode(code);

        // 这里校验集群下code唯一而不是环境下code唯一是因为helm的release是需要集群下唯一的
        if (appServiceInstanceMapper.checkCodeExist(code, envId)) {
            throw new CommonException("error.app.instance.name.already.exist");
        }
    }


    @Override
    public InstanceValueVO getReplaceResult(String versionValue, String deployValue) {
        String deployFileAfterProcessed = deleteLineStartWithPoundKey(deployValue);
        if (versionValue.equals(deployValue) || !org.springframework.util.StringUtils.hasText(deployFileAfterProcessed)) {
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
            LOGGER.warn("Failed to replace values. the version values is {} and deploy value is {}", versionValue, deployValue);
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
        Assert.notNull(code, "error.code.is.null");
        Assert.notNull(envId, "error.envId.is.null");

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
    public List<AppServiceInstanceOverViewDTO> baseListApplicationInstanceOverView(Long projectId, Long
            appServiceId, List<Long> envIds) {
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
    public List<DeployDTO> baseListDeployTime(Long projectId, Long envId, Long[] appServiceIds, Date startTime, Date
            endTime) {
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
    public Page<DeployDTO> basePageDeployFrequencyTable(Long projectId, PageRequest pageable, Long[] envIds, Long
            appServiceId,
                                                        Date startTime, Date endTime) {
        return PageHelper.doPageAndSort(PageRequestUtil.simpleConvertSortForPage(pageable), () ->
                appServiceInstanceMapper
                        .listDeployFrequency(projectId, envIds, appServiceId, new java.sql.Date(startTime.getTime()),
                                new java.sql.Date(endTime.getTime())));
    }

    @Override
    public Page<DeployDTO> basePageDeployTimeTable(Long projectId, PageRequest pageable, Long envId, Long[]
            appServiceIds,
                                                   Date startTime, Date endTime) {
        return PageHelper.doPageAndSort(PageRequestUtil.simpleConvertSortForPage(pageable), () ->
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
        // 删除实例
        AppServiceInstanceDTO appServiceInstanceDTO = new AppServiceInstanceDTO();
        appServiceInstanceDTO.setEnvId(envId);
        appServiceInstanceMapper.delete(appServiceInstanceDTO);
        // 删除实例对应的应用
        DevopsDeployAppCenterEnvDTO devopsDeployAppCenterEnvDTO = new DevopsDeployAppCenterEnvDTO();
        devopsDeployAppCenterEnvDTO.setEnvId(envId);
        devopsDeployAppCenterService.delete(devopsDeployAppCenterEnvDTO);
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

    @Override
    public ConfigVO queryDefaultConfig(Long projectId, ConfigVO configVO) {
        DevopsProjectDTO devopsProjectDTO = devopsProjectMapper.selectByPrimaryKey(projectId);
        if (devopsProjectDTO.getHarborProjectIsPrivate()) {
            configVO.setIsPrivate(true);
            HarborUserDTO harborUserDTO = devopsHarborUserService.queryHarborUserById(devopsProjectDTO.getHarborPullUserId());
            configVO.setUserName(harborUserDTO.getHarborProjectUserName());
            configVO.setPassword(harborUserDTO.getHarborProjectUserPassword());
        }
        return configVO;
    }

    @Override
    public Integer countByOptions(Long envId, String status, Long appServiceId) {
        return appServiceInstanceMapper.countInstanceByCondition(envId, status, appServiceId);
    }

    private InstanceSagaPayload processSingleOfBatch(Long projectId, DevopsEnvironmentDTO
            devopsEnvironmentDTO, UserAttrDTO userAttrDTO, AppServiceDeployVO
                                                             appServiceDeployVO, Map<Long, List<Pair<Long, String>>> envSecrets) {
        //校验values
        FileUtil.checkYamlFormat(appServiceDeployVO.getValues());
        ProjectDTO projectDTO = baseServiceClientOperator.queryIamProjectById(projectId);

        AppServiceDTO appServiceDTO = applicationService.baseQuery(appServiceDeployVO.getAppServiceId());

        if (appServiceDTO == null) {
            throw new CommonException("error.app.service.not.exist");
        }

        if (!Boolean.TRUE.equals(appServiceDTO.getActive())) {
            throw new CommonException("error.app.service.disabled");
        }

        AppServiceVersionDTO appServiceVersionDTO =
                appServiceVersionService.baseQuery(appServiceDeployVO.getAppServiceVersionId());

        //初始化ApplicationInstanceDTO,DevopsEnvCommandDTO,DevopsEnvCommandValueDTO
        AppServiceInstanceDTO appServiceInstanceDTO = initApplicationInstanceDTO(appServiceDeployVO);
        DevopsEnvCommandDTO devopsEnvCommandDTO = initDevopsEnvCommandDTO(appServiceDeployVO);
        DevopsEnvCommandValueDTO devopsEnvCommandValueDTO = initDevopsEnvCommandValueDTO(appServiceDeployVO.getValues());

        //获取部署实例时授权secret的code
        String secretCode = getSecret(appServiceDTO, appServiceDeployVO.getAppServiceVersionId(), devopsEnvironmentDTO, envSecrets.computeIfAbsent(devopsEnvironmentDTO.getId(), k -> new ArrayList<>()));

        // 初始化自定义实例名
        String code;
        if (appServiceDeployVO.getAppCode() == null || appServiceDeployVO.getAppCode().trim().equals("")) {
            code = String.format(INSTANCE_NAME_TEMPLATE, appServiceDTO.getCode(), GenerateUUID.generateUUID().substring(0, 5));
        } else {
            checkNameInternal(appServiceDeployVO.getAppCode(), appServiceDeployVO.getEnvironmentId(), false);
            code = appServiceDeployVO.getAppCode();
        }


        appServiceInstanceDTO.setCode(code);
        appServiceInstanceDTO.setId(baseCreate(appServiceInstanceDTO).getId());
        devopsEnvCommandDTO.setObjectId(appServiceInstanceDTO.getId());
        devopsEnvCommandDTO.setValueId(devopsEnvCommandValueService.baseCreate(devopsEnvCommandValueDTO).getId());
        appServiceInstanceDTO.setCommandId(devopsEnvCommandService.baseCreate(devopsEnvCommandDTO).getId());
        baseUpdate(appServiceInstanceDTO);
        boolean isProjectAppService = devopsEnvironmentDTO.getProjectId().equals(appServiceDTO.getProjectId());
        //存储数据
        ApplicationCenterEnum appSourceType = isProjectAppService ? ApplicationCenterEnum.PROJECT : ApplicationCenterEnum.SHARE;
        String serviceCode = appServiceDTO.getCode();
        String serviceName = appServiceDTO.getName();
        devopsEnvApplicationService.createEnvAppRelationShipIfNon(appServiceDeployVO.getAppServiceId(), appServiceDeployVO.getEnvironmentId(), appSourceType.value(), serviceCode, serviceName);

        DevopsDeployAppCenterEnvDTO devopsDeployAppCenterEnvDTO = devopsDeployAppCenterService.baseCreate(appServiceDeployVO.getAppName(), appServiceDeployVO.getAppCode(), projectId, appServiceInstanceDTO.getId(), appServiceDeployVO.getEnvironmentId(), OperationTypeEnum.BATCH_DEPLOY.value(), isProjectAppService ? AppSourceType.NORMAL.getValue() : AppSourceType.SHARE.getValue(), RdupmTypeEnum.CHART.value());


        //插入部署记录
        devopsDeployRecordService.saveRecord(devopsEnvironmentDTO.getProjectId(),
                DeployType.BATCH,
                devopsEnvCommandDTO.getId(),
                DeployModeEnum.ENV,
                devopsEnvironmentDTO.getId(),
                devopsEnvironmentDTO.getName(),
                null,
                DeployObjectTypeEnum.CHART,
                appServiceDTO.getName(),
                appServiceVersionDTO.getVersion(),
                appServiceDeployVO.getAppName(),
                appServiceDeployVO.getAppCode(),
                devopsDeployAppCenterEnvDTO.getId(),
                new DeploySourceVO(isProjectAppService ? AppSourceType.CURRENT_PROJECT : AppSourceType.SHARE, projectDTO.getName()));

        // 创建应用中心应用

        appServiceDeployVO.setInstanceId(appServiceInstanceDTO.getId());
        appServiceDeployVO.setInstanceName(code);
        if (appServiceDeployVO.getDevopsServiceReqVO() != null) {
            appServiceDeployVO.getDevopsServiceReqVO().setDevopsIngressVO(appServiceDeployVO.getDevopsIngressVO());
        }
        InstanceSagaPayload instanceSagaPayload = new InstanceSagaPayload(devopsEnvironmentDTO.getProjectId(), userAttrDTO.getGitlabUserId(), secretCode, appServiceInstanceDTO.getCommandId());
        instanceSagaPayload.setApplicationDTO(appServiceDTO);
        instanceSagaPayload.setAppServiceVersionDTO(appServiceVersionDTO);
        instanceSagaPayload.setAppServiceDeployVO(appServiceDeployVO);
        instanceSagaPayload.setDevopsEnvironmentDTO(devopsEnvironmentDTO);
        instanceSagaPayload.setDevopsIngressVO(appServiceDeployVO.getDevopsIngressVO());
        instanceSagaPayload.setDevopsServiceReqVO(appServiceDeployVO.getDevopsServiceReqVO());
        return instanceSagaPayload;
    }

    @Saga(code = SagaTopicCodeConstants.DEVOPS_BATCH_DEPLOYMENT, inputSchemaClass = BatchDeploymentPayload.class, description = "批量部署实例")
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
    @Override
    public List<AppServiceInstanceVO> batchDeployment(Long projectId, List<AppServiceDeployVO> appServiceDeployVOS) {
        DevopsEnvironmentDTO devopsEnvironmentDTO = permissionHelper.checkEnvBelongToProject(projectId, appServiceDeployVOS.get(0).getEnvironmentId());

        UserAttrDTO userAttrDTO = userAttrService.baseQueryById(TypeUtil.objToLong(GitUserNameUtil.getUserId()));

        //校验环境相关信息
        devopsEnvironmentService.checkEnv(devopsEnvironmentDTO, userAttrDTO);
        List<InstanceSagaPayload> instances = new ArrayList<>();
        List<ServiceSagaPayLoad> services = new ArrayList<>();
        List<IngressSagaPayload> ingresses = new ArrayList<>();
        List<DevopsDeployRecordInstanceDTO> recordInstances = new ArrayList<>();
        List<Long> instanceIds = new ArrayList<>();

        // 纪录此次批量部署的环境id及要创建的docker_registry_secret的code的映射
        // 环境id -> configId -> secretCode
        Map<Long, List<Pair<Long, String>>> envSecrets = new HashMap<>();
        for (AppServiceDeployVO appServiceDeployVO : appServiceDeployVOS) {
            InstanceSagaPayload payload = processSingleOfBatch(projectId, devopsEnvironmentDTO, userAttrDTO, appServiceDeployVO, envSecrets);
            instances.add(payload);
            recordInstances.add(new DevopsDeployRecordInstanceDTO(
                    null,
                    payload.getAppServiceDeployVO().getInstanceId(),
                    payload.getAppServiceDeployVO().getAppCode(),
                    payload.getAppServiceVersionDTO().getVersion(),
                    payload.getApplicationDTO().getId(),
                    devopsEnvironmentDTO.getId()));
            instanceIds.add(payload.getAppServiceDeployVO().getInstanceId());

            //创建实例时，如果选择了创建网络
            if (appServiceDeployVO.getDevopsServiceReqVO() != null) {
                appServiceDeployVO.getDevopsServiceReqVO().setAppServiceId(payload.getApplicationDTO().getId());
                ServiceSagaPayLoad serviceSagaPayLoad = devopsServiceService.createForBatchDeployment(devopsEnvironmentDTO, userAttrDTO, projectId, appServiceDeployVO.getDevopsServiceReqVO());
                services.add(serviceSagaPayLoad);

                //创建实例时，如果选了创建域名
                if (appServiceDeployVO.getDevopsIngressVO() != null) {
                    appServiceDeployVO.getDevopsIngressVO().setAppServiceId(serviceSagaPayLoad.getDevopsServiceDTO().getTargetAppServiceId());
                    List<DevopsIngressPathVO> devopsIngressPathVOS = appServiceDeployVO.getDevopsIngressVO().getPathList();
                    devopsIngressPathVOS.forEach(devopsIngressPathVO -> {
                        DevopsServiceDTO devopsServiceDTO = devopsServiceService.baseQueryByNameAndEnvId(devopsIngressPathVO.getServiceName(), devopsEnvironmentDTO.getId());
                        if (devopsServiceDTO != null) {
                            devopsIngressPathVO.setServiceId(devopsServiceDTO.getId());
                        }
                    });
                    appServiceDeployVO.getDevopsIngressVO().setPathList(devopsIngressPathVOS);
                    ingresses.add(devopsIngressService.createForBatchDeployment(devopsEnvironmentDTO, userAttrDTO, projectId, appServiceDeployVO.getDevopsIngressVO()));
                }
            }
        }

        // 构造saga的payload
        BatchDeploymentPayload batchDeploymentPayload = new BatchDeploymentPayload();
        batchDeploymentPayload.setEnvId(devopsEnvironmentDTO.getId());
        batchDeploymentPayload.setProjectId(projectId);
        batchDeploymentPayload.setInstanceSagaPayloads(instances);
        batchDeploymentPayload.setServiceSagaPayLoads(services);
        batchDeploymentPayload.setIngressSagaPayloads(ingresses);
        batchDeploymentPayload.setGitlabUserId(TypeUtil.objToInteger(userAttrDTO.getGitlabUserId()));
        batchDeploymentPayload.setIamUserId(userAttrDTO.getIamUserId());

        producer.apply(StartSagaBuilder.newBuilder()
                .withLevel(ResourceLevel.PROJECT)
                .withSourceId(projectId)
                .withRefId(String.valueOf(devopsEnvironmentDTO.getId()))
                .withRefType("env")
                .withSagaCode(SagaTopicCodeConstants.DEVOPS_BATCH_DEPLOYMENT)
                .withJson(new JSON().serialize(batchDeploymentPayload)), LambdaUtil.doNothingConsumer());

        return ConvertUtils.convertList(appServiceInstanceMapper.queryByInstanceIds(instanceIds), AppServiceInstanceVO.class);
    }

    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
    @Override
    public void batchDeploymentSaga(BatchDeploymentPayload batchDeploymentPayload) {
        DevopsEnvironmentDTO devopsEnvironmentDTO = devopsEnvironmentService.baseQueryById(batchDeploymentPayload.getEnvId());
        if (devopsEnvironmentDTO == null) {
            throw new CommonException("error.env.id.not.exist", batchDeploymentPayload.getEnvId());
        }

        Map<String, String> pathContentMap = new HashMap<>();

        List<InstanceSagaPayload> instanceSagaPayloads = batchDeploymentPayload.getInstanceSagaPayloads();
        for (InstanceSagaPayload instanceSagaPayload : instanceSagaPayloads) {
            //在gitops库处理instance文件
            ResourceConvertToYamlHandler<C7nHelmRelease> resourceConvertToYamlHandler = new ResourceConvertToYamlHandler<>();
            resourceConvertToYamlHandler.setType(getC7NHelmRelease(
                    instanceSagaPayload.getAppServiceDeployVO().getInstanceName(),
                    instanceSagaPayload.getAppServiceVersionDTO().getRepository(),
                    instanceSagaPayload.getApplicationDTO().getId(),
                    instanceSagaPayload.getCommandId(),
                    instanceSagaPayload.getApplicationDTO().getCode(),
                    instanceSagaPayload.getAppServiceVersionDTO().getVersion(),
                    instanceSagaPayload.getAppServiceDeployVO().getValues(),
                    instanceSagaPayload.getAppServiceDeployVO().getAppServiceVersionId(),
                    instanceSagaPayload.getSecretCode(),
                    instanceSagaPayload.getDevopsEnvironmentDTO()));

            String instanceContent = resourceConvertToYamlHandler.getCreationResourceContentForBatchDeployment();
            String fileName = GitOpsConstants.RELEASE_PREFIX + instanceSagaPayload.getAppServiceDeployVO().getInstanceName() + GitOpsConstants.YAML_FILE_SUFFIX;
            pathContentMap.put(fileName, instanceContent);
        }

        for (ServiceSagaPayLoad serviceSagaPayLoad : batchDeploymentPayload.getServiceSagaPayLoads()) {
            ResourceConvertToYamlHandler<V1Service> resourceConvertToYamlHandler = new ResourceConvertToYamlHandler<>();
            resourceConvertToYamlHandler.setType(serviceSagaPayLoad.getV1Service());
            String serviceContent = resourceConvertToYamlHandler.getCreationResourceContentForBatchDeployment();
            String fileName = GitOpsConstants.SERVICE_PREFIX + serviceSagaPayLoad.getDevopsServiceDTO().getName() + GitOpsConstants.YAML_FILE_SUFFIX;
            pathContentMap.put(fileName, serviceContent);
        }

        for (IngressSagaPayload ingressSagaPayload : batchDeploymentPayload.getIngressSagaPayloads()) {
            ResourceConvertToYamlHandler<V1beta1Ingress> ingressResourceConvertToYamlHandler = new ResourceConvertToYamlHandler<>();
            ingressResourceConvertToYamlHandler.setType(ingressSagaPayload.getV1beta1Ingress());
            String ingressContent = ingressResourceConvertToYamlHandler.getCreationResourceContentForBatchDeployment();
            String fileName = GitOpsConstants.INGRESS_PREFIX + ingressSagaPayload.getDevopsIngressDTO().getName() + GitOpsConstants.YAML_FILE_SUFFIX;
            pathContentMap.put(fileName, ingressContent);
        }

        gitlabServiceClientOperator.createGitlabFiles(
                TypeUtil.objToInteger(devopsEnvironmentDTO.getGitlabEnvProjectId()),
                batchDeploymentPayload.getGitlabUserId(),
                GitOpsConstants.MASTER,
                pathContentMap,
                GitOpsConstants.BATCH_DEPLOYMENT_COMMIT_MESSAGE);
    }


    @Override
    public AppServiceVersionDTO queryVersion(Long appServiceInstanceId) {
        return appServiceInstanceMapper.queryVersion(appServiceInstanceId);
    }

    @Override
    public List<ApplicationInstanceInfoVO> listByServiceAndEnv(Long projectId, Long appServiceId, Long envId, boolean withPodInfo) {
        List<ApplicationInstanceInfoVO> applicationInstanceInfoVOS = appServiceInstanceMapper.listAppInstanceByAppSvcIdAndEnvId(appServiceId, envId);
        if (CollectionUtils.isEmpty(applicationInstanceInfoVOS)) {
            return new ArrayList<>();
        }
        if (withPodInfo) {
            applicationInstanceInfoVOS.forEach(applicationInstanceInfoVO -> {
                List<DevopsEnvPodDTO> devopsEnvPodDTOS = devopsEnvPodService.baseListByInstanceId(applicationInstanceInfoVO.getId());
                if (CollectionUtils.isEmpty(devopsEnvPodDTOS)) {
                    applicationInstanceInfoVO.setPodCount(0);
                    applicationInstanceInfoVO.setPodRunningCount(0);
                } else {
                    long count = devopsEnvPodDTOS.stream().filter(v -> Boolean.TRUE.equals(v.getReady())).count();
                    applicationInstanceInfoVO.setPodCount(devopsEnvPodDTOS.size());
                    applicationInstanceInfoVO.setPodRunningCount((int) count);
                }
            });
        }

        return applicationInstanceInfoVOS;
    }

    @Override
    @Async
    @Transactional(rollbackFor = Exception.class)
    public void hzeroDeploy(Long detailsRecordId) {
        LOGGER.info(">>>>>>> Start deploy hzero app, detailsRecordId : {} <<<<<<<", detailsRecordId);
        DevopsHzeroDeployDetailsDTO devopsHzeroDeployDetailsDTO = devopsHzeroDeployDetailsService.baseQueryById(detailsRecordId);
        DevopsDeployRecordDTO devopsDeployRecordDTO = devopsDeployRecordService.baseQueryById(devopsHzeroDeployDetailsDTO.getDeployRecordId());

        if (!HzeroDeployDetailsStatusEnum.CREATED.value().equals(devopsHzeroDeployDetailsDTO.getStatus())) {
            LOGGER.info(">>>>>>> detailsRecord status not create, skip : {} <<<<<<<", detailsRecordId);
            return;
        }
        devopsHzeroDeployDetailsDTO.setStartTime(new Date());
        try {
            ApplicationContextHelper
                    .getContext()
                    .getBean(AppServiceInstanceService.class)
                    .pipelineDeployHzeroApp(devopsDeployRecordDTO.getProjectId(), devopsHzeroDeployDetailsDTO);
        } catch (Exception e) {
            LOGGER.info(">>>>>>> Deploy hzero app failed ! <<<<<<<", e);
            devopsHzeroDeployDetailsDTO.setEndTime(new Date());
            devopsHzeroDeployDetailsDTO.setStatus(HzeroDeployDetailsStatusEnum.FAILED.value());
            workFlowServiceOperator.stopInstance(devopsDeployRecordDTO.getProjectId(), devopsDeployRecordDTO.getBusinessKey());
            devopsHzeroDeployDetailsService.baseUpdate(devopsHzeroDeployDetailsDTO);
            devopsDeployRecordService.updateResultById(devopsHzeroDeployDetailsDTO.getDeployRecordId(), DeployResultEnum.FAILED);
        }

    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void pipelineDeployHzeroApp(Long projectId, DevopsHzeroDeployDetailsDTO devopsHzeroDeployDetailsDTO) {

//        AppServiceInstanceDTO appServiceInstanceDTO = baseQueryByCodeAndEnv(devopsHzeroDeployDetailsDTO.getInstanceCode(), devopsHzeroDeployDetailsDTO.getEnvId());
        DevopsHzeroDeployConfigDTO devopsHzeroDeployConfigDTO = devopsHzeroDeployConfigService.baseQueryById(devopsHzeroDeployDetailsDTO.getValueId());

        // 设置用户上下文
        CustomUserDetails customUserDetails = new CustomUserDetails("default", "default");
        customUserDetails.setUserId(devopsHzeroDeployDetailsDTO.getCreatedBy());
        customUserDetails.setOrganizationId(BaseConstants.DEFAULT_TENANT_ID);
        customUserDetails.setLanguage(BaseConstants.DEFAULT_LOCALE_STR);

        DetailsHelper.setCustomUserDetails(customUserDetails);
        AppServiceInstanceVO instanceVO;
        Long commandId = null;
        if (devopsHzeroDeployDetailsDTO.getAppId() == null) {
            // 新建实例
            MarketInstanceCreationRequestVO marketInstanceCreationRequestVO = new MarketInstanceCreationRequestVO(
                    null,
                    devopsHzeroDeployDetailsDTO.getMktServiceId(),
                    devopsHzeroDeployDetailsDTO.getMktDeployObjectId(),
                    devopsHzeroDeployConfigDTO.getValue(),
                    devopsHzeroDeployDetailsDTO.getAppName(),
                    devopsHzeroDeployDetailsDTO.getAppCode(),
                    CommandType.CREATE.getType(),
                    devopsHzeroDeployDetailsDTO.getEnvId(),
                    devopsHzeroDeployConfigDTO.getService() == null ? null : JsonHelper.unmarshalByJackson(devopsHzeroDeployConfigDTO.getService(), DevopsServiceReqVO.class),
                    devopsHzeroDeployConfigDTO.getIngress() == null ? null : JsonHelper.unmarshalByJackson(devopsHzeroDeployConfigDTO.getIngress(), DevopsIngressVO.class),
                    AppSourceType.MARKET.getValue(),
                    AppSourceType.HZERO.getValue(),
                    OperationTypeEnum.HZERO.value());
            marketInstanceCreationRequestVO.setInstanceName(devopsHzeroDeployDetailsDTO.getAppCode());
            instanceVO = createOrUpdateMarketInstance(projectId, marketInstanceCreationRequestVO, false);
            commandId = instanceVO.getCommandId();
            devopsHzeroDeployDetailsDTO.setAppId(instanceVO.getAppId());
        } else {
            DevopsDeployAppCenterEnvDTO devopsDeployAppCenterEnvDTO = devopsDeployAppCenterService.selectByPrimaryKey(devopsHzeroDeployDetailsDTO.getAppId());
            AppServiceInstanceDTO appServiceInstanceDTO = baseQuery(devopsDeployAppCenterEnvDTO.getObjectId());
            // 更新实例
            if (devopsHzeroDeployDetailsDTO.getMktDeployObjectId().equals(appServiceInstanceDTO.getAppServiceVersionId())
                    && baseQueryValueByInstanceId(appServiceInstanceDTO.getId()).equals(devopsHzeroDeployConfigDTO.getValue())) {
                // 版本和配置相同则走重新部署的逻辑
                DevopsEnvCommandDTO devopsEnvCommandDTO = restartInstance(projectId,
                        appServiceInstanceDTO.getId(),
                        false,
                        false);
                commandId = devopsEnvCommandDTO.getId();
            } else {
                MarketInstanceCreationRequestVO marketInstanceCreationRequestVO = new MarketInstanceCreationRequestVO(
                        appServiceInstanceDTO.getId(),
                        devopsHzeroDeployDetailsDTO.getMktServiceId(),
                        devopsHzeroDeployDetailsDTO.getMktDeployObjectId(),
                        devopsHzeroDeployConfigDTO.getValue(),
                        devopsHzeroDeployDetailsDTO.getAppName(),
                        devopsHzeroDeployDetailsDTO.getAppCode(),
                        CommandType.UPDATE.getType(),
                        devopsHzeroDeployDetailsDTO.getEnvId(),
                        devopsHzeroDeployConfigDTO.getService() == null ? null : JsonHelper.unmarshalByJackson(devopsHzeroDeployConfigDTO.getService(), DevopsServiceReqVO.class),
                        devopsHzeroDeployConfigDTO.getIngress() == null ? null : JsonHelper.unmarshalByJackson(devopsHzeroDeployConfigDTO.getIngress(), DevopsIngressVO.class),
                        AppSourceType.MARKET.getValue(),
                        AppSourceType.HZERO.getValue(),
                        OperationTypeEnum.HZERO.value());
                marketInstanceCreationRequestVO.setInstanceName(devopsHzeroDeployDetailsDTO.getAppCode());
                marketInstanceCreationRequestVO.setInstanceId(appServiceInstanceDTO.getId());
                instanceVO = createOrUpdateMarketInstance(projectId, marketInstanceCreationRequestVO, false);
                commandId = instanceVO.getCommandId();
                devopsHzeroDeployDetailsDTO.setAppId(instanceVO.getAppId());
            }

        }
        // 更新状态以及操作记录
        devopsHzeroDeployDetailsDTO.setStatus(HzeroDeployDetailsStatusEnum.DEPLOYING.value());
        devopsHzeroDeployDetailsDTO.setCommandId(commandId);
        devopsHzeroDeployDetailsDTO.setStartTime(new Date());
        devopsHzeroDeployDetailsDTO.setEndTime(null);

        devopsHzeroDeployDetailsService.baseUpdate(devopsHzeroDeployDetailsDTO);
    }

    @Override
    public List<AppServiceInstanceDTO> listInstanceByDeployDetailsCode(List<String> codes, Long envId) {
        return appServiceInstanceMapper.listInstanceByDeployDetailsCode(codes, envId);
    }

    @Override
    public String queryInstanceStatusByEnvIdAndCode(String code, Long envId) {
        return appServiceInstanceMapper.queryInstanceStatusByEnvIdAndCode(code, envId);
    }

    @Override
    public Integer countInstance() {
        return appServiceInstanceMapper.countInstance();
    }

    @Override
    public List<AppServiceInstanceDTO> listInstances() {
        return appServiceInstanceMapper.listInstances();
    }

    private void handleStartOrStopInstance(Long projectId, Long instanceId, String type) {

        AppServiceInstanceDTO appServiceInstanceDTO = baseQuery(instanceId);

        DevopsEnvironmentDTO devopsEnvironmentDTO = permissionHelper.checkEnvBelongToProject(projectId, appServiceInstanceDTO.getEnvId());

        UserAttrDTO userAttrDTO = userAttrService.baseQueryById(TypeUtil.objToLong(GitUserNameUtil.getUserId()));

        //校验环境相关信息
        devopsEnvironmentService.checkEnv(devopsEnvironmentDTO, userAttrDTO);
        DevopsEnvCommandDTO devopsEnvCommandDTO = devopsEnvCommandService
                .baseQueryByObject(ObjectType.INSTANCE.getType(), instanceId);
        devopsEnvCommandDTO.setCommandType(type);
        devopsEnvCommandDTO.setStatus(CommandStatus.OPERATING.getStatus());
        devopsEnvCommandDTO.setId(null);
        if (CommandType.RESTART.getType().equals(type)) {
            if (!appServiceInstanceDTO.getStatus().equals(InstanceStatus.STOPPED.getStatus())) {
                throw new CommonException("error.instance.not.stop");
            }
            sendNotificationService.sendInstanceStatusUpdate(appServiceInstanceDTO, devopsEnvCommandDTO, appServiceInstanceDTO.getStatus());
        } else {
            if (!appServiceInstanceDTO.getStatus().equals(InstanceStatus.RUNNING.getStatus())) {
                throw new CommonException("error.instance.not.running");
            }
            sendNotificationService.sendInstanceStatusUpdate(appServiceInstanceDTO, devopsEnvCommandDTO, appServiceInstanceDTO.getStatus());
        }
        devopsEnvCommandDTO = devopsEnvCommandService.baseCreate(devopsEnvCommandDTO);
        updateInstanceStatus(instanceId, devopsEnvCommandDTO.getId(), InstanceStatus.OPERATING.getStatus());

        // 如果开启了应用监控，同时关闭应用监控
        DevopsDeployAppCenterEnvDTO devopsDeployAppCenterEnvDTO = devopsDeployAppCenterService.queryByRdupmTypeAndObjectId(RdupmTypeEnum.CHART, instanceId);
        devopsDeployAppCenterService.disableMetric(projectId, devopsDeployAppCenterEnvDTO.getId());

        //发送重启或停止实例的command
        Map<String, String> stopMap = new HashMap<>();
        stopMap.put(RELEASE_NAME, appServiceInstanceDTO.getCode());
        stopMap.put(NAMESPACE, devopsEnvironmentDTO.getCode());
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

    private C7nHelmRelease getC7NHelmRelease(String code, String repository,
                                             Long appServiceId,
                                             Long commandId, String appServiceCode,
                                             String version, String deployValue,
                                             Long deployVersionId, String secretName,
                                             DevopsEnvironmentDTO devopsEnvironmentDTO) {
        C7nHelmRelease c7nHelmRelease = new C7nHelmRelease();
        c7nHelmRelease.getMetadata().setName(code);
        // 设置这个app-service-id是防止不同项目的应用服务被网络根据应用服务code误选择，要以id作为标签保证准确性
        c7nHelmRelease.getSpec().setAppServiceId(appServiceId);
        c7nHelmRelease.getSpec().setRepoUrl(repository);
        c7nHelmRelease.getSpec().setChartName(appServiceCode);
        c7nHelmRelease.getSpec().setChartVersion(version);
        c7nHelmRelease.getSpec().setCommandId(commandId);
        c7nHelmRelease.getSpec().setSource(AppSourceType.NORMAL.getValue());
        c7nHelmRelease.getSpec().setV1CommandId(String.valueOf(commandId));
        c7nHelmRelease.getSpec().setV1AppServiceId(String.valueOf(appServiceId));


        if (secretName != null) {
            c7nHelmRelease.getSpec().setImagePullSecrets(ArrayUtil.singleAsList(new ImagePullSecret(secretName)));
        }

        // 如果是组件的实例进行部署
        String versionValue;
        if (EnvironmentType.SYSTEM.getValue().equals(devopsEnvironmentDTO.getType())) {
            // 设置集群组件的特殊元数据
            c7nHelmRelease.getMetadata().setType(C7NHelmReleaseMetadataType.CLUSTER_COMPONENT.getType());

            versionValue = ComponentVersionUtil.getComponentVersion(appServiceCode).getValues();
        } else {
            versionValue = appServiceVersionService.baseQueryValue(deployVersionId);
        }

        c7nHelmRelease.getSpec().setValues(
                getReplaceResult(versionValue, deployValue).getDeltaYaml().trim());
        return c7nHelmRelease;
    }

    private C7nHelmRelease getC7NHelmReleaseForMarketServiceInstance(String code, String repository,
                                                                     Long appServiceId,
                                                                     Long commandId, String appServiceCode,
                                                                     String version, String deployValue,
                                                                     Long marketServiceVersionId, String secretName,
                                                                     String source,
                                                                     DevopsEnvironmentDTO devopsEnvironmentDTO) {
        C7nHelmRelease c7nHelmRelease = new C7nHelmRelease();
        c7nHelmRelease.getMetadata().setName(code);
        // 设置这个app-service-id是防止不同项目的应用服务被网络根据应用服务code误选择，要以id作为标签保证准确性
        c7nHelmRelease.getSpec().setAppServiceId(appServiceId);
        c7nHelmRelease.getSpec().setRepoUrl(repository);
        c7nHelmRelease.getSpec().setChartName(appServiceCode);
        c7nHelmRelease.getSpec().setChartVersion(version);
        c7nHelmRelease.getSpec().setCommandId(commandId);
        c7nHelmRelease.getSpec().setSource(source);
        c7nHelmRelease.getSpec().setMarketDeployObjectId(marketServiceVersionId);
        c7nHelmRelease.getSpec().setV1CommandId(String.valueOf(commandId));
        c7nHelmRelease.getSpec().setV1AppServiceId(String.valueOf(appServiceId));
        // 密钥名称不为空且来源是MARKET
        if (!StringUtils.isEmpty(secretName) && isMarket(source)) {
            c7nHelmRelease.getSpec().setImagePullSecrets(ArrayUtil.singleAsList(new ImagePullSecret(secretName)));
        }

        // 从market-service查询values
        String versionValue = marketServiceClientOperator.queryValues(devopsEnvironmentDTO.getProjectId(), marketServiceVersionId).getValue();

        c7nHelmRelease.getSpec().setValues(
                getReplaceResult(versionValue, deployValue).getDeltaYaml().trim());
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
        appServiceInstanceDTO.setSource(AppSourceType.NORMAL.getValue());
        if (appServiceDeployVO.getType().equals(UPDATE)) {
            AppServiceInstanceDTO oldAppServiceInstanceDTO = baseQuery(
                    appServiceDeployVO.getInstanceId());
            appServiceInstanceDTO.setCode(oldAppServiceInstanceDTO.getCode());
            appServiceInstanceDTO.setId(appServiceDeployVO.getInstanceId());
        }
        return appServiceInstanceDTO;
    }

    private AppServiceInstanceDTO initMarketInstanceDTO(MarketInstanceCreationRequestVO appServiceDeployVO) {
        AppServiceInstanceDTO appServiceInstanceDTO = new AppServiceInstanceDTO();
        appServiceInstanceDTO.setAppServiceId(appServiceDeployVO.getMarketAppServiceId());
        appServiceInstanceDTO.setEnvId(appServiceDeployVO.getEnvironmentId());
        appServiceInstanceDTO.setStatus(InstanceStatus.OPERATING.getStatus());
        appServiceInstanceDTO.setSource(appServiceDeployVO.getSource());
        if (CommandType.UPDATE.getType().equals(appServiceDeployVO.getCommandType())) {
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

    private DevopsEnvCommandDTO initMarketInstanceDevopsEnvCommandDTO(MarketInstanceCreationRequestVO
                                                                              appServiceDeployVO) {
        DevopsEnvCommandDTO devopsEnvCommandDTO = new DevopsEnvCommandDTO();
        switch (appServiceDeployVO.getCommandType()) {
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
        devopsEnvCommandDTO.setObjectVersionId(appServiceDeployVO.getMarketDeployObjectId());
        devopsEnvCommandDTO.setObject(ObjectType.INSTANCE.getType());
        devopsEnvCommandDTO.setStatus(CommandStatus.OPERATING.getStatus());
        return devopsEnvCommandDTO;
    }

    private DevopsEnvCommandValueDTO initDevopsEnvCommandValueDTO
            (String values) {
        DevopsEnvCommandValueDTO devopsEnvCommandValueDTO = new DevopsEnvCommandValueDTO();
        devopsEnvCommandValueDTO.setValue(values);
        return devopsEnvCommandValueDTO;
    }

    /**
     * 获取用于拉取此版本镜像的secret名称, 如果不需要secret, 返回null. 如果需要, 会创建并返回secret code
     *
     * @param appServiceDTO        应用服务信息
     * @param appServiceVersionId  应用服务版本id
     * @param devopsEnvironmentDTO 环境信息
     * @return secret的code(如果需要)
     */
    @Nullable
    @Override
    public String getSecret(AppServiceDTO appServiceDTO, Long appServiceVersionId, DevopsEnvironmentDTO
            devopsEnvironmentDTO) {
        return getSecret(appServiceDTO, appServiceVersionId, devopsEnvironmentDTO, null);
    }

    /**
     * 获取用于拉取此版本镜像的secret名称, 如果不需要secret, 返回null. 如果需要, 会创建并返回secret code
     *
     * @param appServiceDTO        应用服务信息
     * @param appServiceVersionId  应用服务版本id
     * @param devopsEnvironmentDTO 环境信息
     * @param existedConfigs       这个环境已经存在的secret的config的id，用于避免批量部署在同一个环境为同一个config
     *                             创建相同的secret.
     *                             只有config的id在这个列表中不存在， 才创建secret纪录
     * @return secret的code(如果需要)
     */
    @Nullable
    private String getSecret(AppServiceDTO appServiceDTO, Long appServiceVersionId, DevopsEnvironmentDTO
            devopsEnvironmentDTO, @Nullable List<Pair<Long, String>> existedConfigs) {
        LOGGER.debug("Get secret for app service with id {} and code {} and version id: {}", appServiceDTO.getId(), appServiceDTO.getCode(), appServiceVersionId);
        String secretCode = null;
        //如果应用绑定了私有镜像库,则处理secret
        AppServiceVersionDTO appServiceVersionDTO = appServiceVersionService.baseQuery(appServiceVersionId);

        // 先处理chart的认证信息
        sendChartMuseumAuthentication(devopsEnvironmentDTO.getClusterId(), appServiceDTO, appServiceVersionDTO);

        DevopsConfigDTO devopsConfigDTO;
        if (appServiceVersionDTO.getHarborConfigId() != null) {
            devopsConfigDTO = harborService.queryRepoConfigByIdToDevopsConfig(appServiceDTO.getId(), appServiceDTO.getProjectId(),
                    appServiceVersionDTO.getHarborConfigId(), appServiceVersionDTO.getRepoType(), AUTHTYPE);
        } else {
            //查询harbor的用户名密码
            devopsConfigDTO = harborService.queryRepoConfigToDevopsConfig(appServiceDTO.getProjectId(),
                    appServiceDTO.getId(), AUTHTYPE);
        }
        LOGGER.debug("Docker config for app service with id {} and code {} and version id: {} is not null. And the config id is {}...", appServiceDTO.getId(), appServiceDTO.getCode(), appServiceVersionId, devopsConfigDTO.getId());

        ConfigVO configVO = gson.fromJson(devopsConfigDTO.getConfig(), ConfigVO.class);
        if (configVO.getIsPrivate() != null && configVO.getIsPrivate()) {
            LOGGER.debug("Docker config for app service with id {} and code {} and version id: {} is private.", appServiceDTO.getId(), appServiceDTO.getCode(), appServiceVersionId);

            DevopsRegistrySecretDTO devopsRegistrySecretDTO = devopsRegistrySecretService.baseQueryByClusterIdAndNamespace(devopsEnvironmentDTO.getClusterId(), devopsEnvironmentDTO.getCode(), devopsConfigDTO.getId(), appServiceDTO.getProjectId());
            if (devopsRegistrySecretDTO == null) {
                // 如果调用方是批量部署， 此次批量部署之前的实例创建了secret，不重复创建，直接返回已有的
                // 只有批量部署的这个列表是非空的
                if (!CollectionUtils.isEmpty(existedConfigs)) {
                    for (Pair<Long, String> configAndSecret : existedConfigs) {
                        if (Objects.equals(configAndSecret.getFirst(), devopsConfigDTO.getId())) {
                            LOGGER.info("Got existed secret {} from list...", configAndSecret.getSecond());
                            return configAndSecret.getSecond();
                        }
                    }
                }

                //当配置在当前环境下没有创建过secret.则新增secret信息，并通知k8s创建secret
                secretCode = String.format("%s%s", "secret-", GenerateUUID.generateUUID().substring(0, 20));
                // 测试应用的secret是没有环境id的，此处环境id只是暂存，之后不使用，考虑后续版本删除此字段
                devopsRegistrySecretDTO = new DevopsRegistrySecretDTO(devopsEnvironmentDTO.getId(), devopsConfigDTO.getId(), devopsEnvironmentDTO.getCode(), devopsEnvironmentDTO.getClusterId(), secretCode, gson.toJson(configVO), appServiceDTO.getProjectId(), devopsConfigDTO.getType());
                devopsRegistrySecretService.createIfNonInDb(devopsRegistrySecretDTO);
                agentCommandService.operateSecret(devopsEnvironmentDTO.getClusterId(), devopsEnvironmentDTO.getCode(), secretCode, configVO);

                // 更新列表
                if (existedConfigs != null) {
                    Pair<Long, String> newPair = new Pair<>(devopsConfigDTO.getId(), secretCode);
                    existedConfigs.add(newPair);
                    LOGGER.info("Docker registry pair added. It is {}. The current list size is {}", newPair, existedConfigs.size());
                }
            } else {
                //判断如果某个配置有发生过修改，则需要修改secret信息，并通知k8s更新secret
                if (!devopsRegistrySecretDTO.getSecretDetail().equals(gson.toJson(configVO))) {
                    devopsRegistrySecretDTO.setSecretDetail(gson.toJson(configVO));
                    devopsRegistrySecretService.baseUpdate(devopsRegistrySecretDTO);
                }
                // 无论是否修改，都通知agent创建secret，保证secret存在
                // 解决secret在Kubernetes集群中被删除而猪齿鱼无法感知的问题
                agentCommandService.operateSecret(devopsEnvironmentDTO.getClusterId(), devopsEnvironmentDTO.getCode(), devopsRegistrySecretDTO.getSecretCode(), configVO);
                secretCode = devopsRegistrySecretDTO.getSecretCode();
            }
        }


        LOGGER.debug("Got secret with code {} for app service with id {} and code {} and version id: {}", secretCode, appServiceDTO.getId(), appServiceDTO.getCode(), appServiceVersionId);
        return secretCode;
    }

    @Override
    public String makeMarketSecret(Long projectId, DevopsEnvironmentDTO
            devopsEnvironmentDTO, MarketServiceDeployObjectVO marketServiceDeployObjectVO) {
        // 先处理chart的认证信息
        sendChartMuseumAuthForMarket(devopsEnvironmentDTO.getClusterId(), marketServiceDeployObjectVO.getMarketChartConfigVO());
        // 处理镜像的认证
        DevopsRegistrySecretDTO devopsRegistrySecretDTO = devopsRegistrySecretService.baseQueryByClusterIdAndNamespace(devopsEnvironmentDTO.getClusterId(), devopsEnvironmentDTO.getCode(), marketServiceDeployObjectVO.getHarborConfigId(), projectId);
        String secretCode;
        if (devopsRegistrySecretDTO == null) {
            //当配置在当前环境下没有创建过secret.则新增secret信息，并通知k8s创建secret
            secretCode = String.format("%s%s", "secret-", GenerateUUID.generateUUID().substring(0, 20));
            MarketHarborConfigVO marketHarborConfigVO = marketServiceDeployObjectVO.getMarketHarborConfigVO();
            ConfigVO configVO = new ConfigVO();
            String[] harborUrlAndRepo = parseMarketRepo(marketHarborConfigVO.getRepoUrl());
            configVO.setUrl(harborUrlAndRepo[0]);
            configVO.setProject(harborUrlAndRepo[1]);
            configVO.setUserName(marketHarborConfigVO.getRobotName());
            configVO.setPassword(marketHarborConfigVO.getToken());
            devopsRegistrySecretDTO = new DevopsRegistrySecretDTO(devopsEnvironmentDTO.getId(), marketServiceDeployObjectVO.getHarborConfigId(), devopsEnvironmentDTO.getCode(), devopsEnvironmentDTO.getClusterId(), secretCode, gson.toJson(configVO), projectId, DevopsRegistryRepoType.MARKET_REPO.getType());
            devopsRegistrySecretService.createIfNonInDb(devopsRegistrySecretDTO);
            agentCommandService.operateSecret(devopsEnvironmentDTO.getClusterId(), devopsEnvironmentDTO.getCode(), secretCode, configVO);
        }
        // 市场服务的harbor config不会更新，所以这里的secret也不考虑更新
        secretCode = devopsRegistrySecretDTO.getSecretCode();
        agentCommandService.operateSecret(devopsEnvironmentDTO.getClusterId(), devopsEnvironmentDTO.getCode(), devopsRegistrySecretDTO.getSecretCode(), gson.fromJson(devopsRegistrySecretDTO.getSecretDetail(), ConfigVO.class));
        return secretCode;
    }

    @Override
    public InstanceValueVO queryValues(Long instanceId) {
        AppServiceInstanceDTO appServiceInstanceDTO = baseQuery(instanceId);
        // 上次实例部署时的完整values
        String yaml = FileUtil.checkValueFormat(baseQueryValueByInstanceId(instanceId));

        InstanceValueVO instanceValueVO = new InstanceValueVO();
        fillDeployValueInfo(instanceValueVO, appServiceInstanceDTO.getValueId());
        instanceValueVO.setYaml(yaml);
        return instanceValueVO;
    }

    @Override
    public InstanceValueVO queryValueForMarketInstance(Long projectId, Long instanceId, Long marketDeployObjectId) {
        AppServiceInstanceDTO appServiceInstanceDTO = baseQuery(instanceId);
        CommonExAssertUtil.assertNotNull(appServiceInstanceDTO, "instance.not.exist.in.database");
        InstanceValueVO instanceValueVO = new InstanceValueVO();
        instanceValueVO.setYaml(marketServiceClientOperator.queryValues(projectId, marketDeployObjectId).getValue());
        return instanceValueVO;
    }

    private String[] parseMarketRepo(String harborRepo) {
        if (harborRepo.endsWith(BaseConstants.Symbol.SLASH)) {
            harborRepo = harborRepo.substring(0, harborRepo.length() - 1);
        }
        int lastSlashIndex = harborRepo.lastIndexOf(BaseConstants.Symbol.SLASH);
        CommonExAssertUtil.assertTrue(lastSlashIndex != -1, "error.harbor.repo.invalid.slash");
        String harborUrl = harborRepo.substring(0, lastSlashIndex);
        String repoName = harborRepo.substring(lastSlashIndex);
        CommonExAssertUtil.assertTrue(harborUrl.contains("//"), "error.harbor.repo.invalid.double.slash");
        String[] result = new String[2];
        result[0] = harborUrl;
        result[1] = repoName;
        return result;
    }

    private String parseMarketChartRepo(String chartRepo) {
        if (chartRepo.endsWith(BaseConstants.Symbol.SLASH)) {
            chartRepo = chartRepo.substring(0, chartRepo.length() - 1);
        }

        int lastSlashIndex = chartRepo.lastIndexOf(BaseConstants.Symbol.SLASH);
        CommonExAssertUtil.assertTrue(lastSlashIndex != -1, "error.chart.repo.invalid.slash");
        String tempUrl = chartRepo.substring(0, lastSlashIndex);
        lastSlashIndex = tempUrl.lastIndexOf(BaseConstants.Symbol.SLASH);

        String repoName = chartRepo.substring(0, lastSlashIndex);
        CommonExAssertUtil.assertTrue(chartRepo.contains("//"), "error.chart.repo.invalid.double.slash");
        return repoName;
    }

    /**
     * 发送chart museum的认证信息
     *
     * @param clusterId            集群id
     * @param appServiceDTO        应用服务
     * @param appServiceVersionDTO 应用服务版本
     */
    private void sendChartMuseumAuthentication(Long clusterId, AppServiceDTO appServiceDTO, AppServiceVersionDTO
            appServiceVersionDTO) {
        if (appServiceVersionDTO.getHelmConfigId() != null) {
            // 查询chart配置
            DevopsConfigDTO devopsConfigDTO = devopsConfigService.queryRealConfig(appServiceDTO.getId(), APP_SERVICE, "chart", null);
            ConfigVO helmConfig = gson.fromJson(devopsConfigDTO.getConfig(), ConfigVO.class);
            // 如果是私有的, 发送认证信息给agent
            if (Boolean.TRUE.equals(helmConfig.getIsPrivate())) {
                agentCommandService.sendChartMuseumAuthentication(clusterId, helmConfig);
            }
        }
    }

    private void sendChartMuseumAuthForMarket(Long clusterId, MarketChartConfigVO marketChartConfigVO) {
        ConfigVO configVO = new ConfigVO();
        String domainAndRepo = parseMarketChartRepo(marketChartConfigVO.getRepoUrl());
        configVO.setUrl(domainAndRepo);
        configVO.setUserName(marketChartConfigVO.getUsername());
        configVO.setPassword(marketChartConfigVO.getPassword());
        if (configVO.getUserName() != null && configVO.getPassword() != null) {
            agentCommandService.sendChartMuseumAuthentication(clusterId, configVO);
        }
    }

    /**
     * filter the pods that are associated with the daemonSet.
     *
     * @param devopsEnvPodVOS the pods to be filtered
     * @param resourceName    the name of resource
     * @param kind            the resource kind
     * @return the pods
     */
    private List<DevopsEnvPodVO> filterPodsAssociatedWithResource(List<DevopsEnvPodVO> devopsEnvPodVOS, String resourceName, String kind) {
        return devopsEnvPodVOS
                .stream()
                .filter(
                        devopsEnvPodVO -> devopsEnvPodVO.getOwnerKind().equals(kind) && resourceName.equals(devopsEnvPodVO.getName().substring(0, devopsEnvPodVO.getName().lastIndexOf('-')))
                )
                .collect(Collectors.toList());
    }

    private Page<DeployDetailTableVO> getDeployDetailDTOS(Page<DeployDTO> deployDTOPageInfo) {

        Page<DeployDetailTableVO> pageDeployDetailDTOS = ConvertUtils.convertPage(deployDTOPageInfo, DeployDetailTableVO.class);

        List<DeployDetailTableVO> deployDetailTableVOS = new ArrayList<>();

        deployDTOPageInfo.getContent().forEach(deployDTO -> {
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
        pageDeployDetailDTOS.setContent(deployDetailTableVOS);
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
     * @param devopsEnvPodVOS the pods to be filtered
     * @param deploymentName  the name of deployment
     * @return the pods
     */
    private List<DevopsEnvPodVO> filterPodsAssociated(List<DevopsEnvPodVO> devopsEnvPodVOS, String
            deploymentName) {
        return devopsEnvPodVOS.stream().filter(devopsEnvPodVO -> {
                    if (ResourceType.REPLICASET.getType().equals(devopsEnvPodVO.getOwnerKind())) {
                        String controllerNameFromPod = devopsEnvPodVO.getName().substring(0,
                                devopsEnvPodVO.getName().lastIndexOf('-', devopsEnvPodVO.getName().lastIndexOf('-') - 1));
                        return deploymentName.equals(controllerNameFromPod);
                    } else {
                        return false;
                    }
                }
        ).collect(Collectors.toList());
    }

    public static boolean isMiddleware(String source) {
        return AppSourceType.MIDDLEWARE.getValue().equals(source);
    }

    private String deleteLineStartWithPoundKey(String value) {
        if (!org.springframework.util.StringUtils.hasText(value)) {
            return value;
        }
        String[] strings = value.split("\n");
        StringBuilder sb = new StringBuilder();
        for (String string : strings) {
            if (!string.trim().startsWith("#")) {
                sb.append(string).append("\n");
            }
        }
        return sb.toString();
    }

    public static boolean isMarket(String source) {
        return AppSourceType.MARKET.getValue().equals(source);
    }
}
