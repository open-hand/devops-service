package io.choerodon.devops.app.service.impl;

import static io.choerodon.devops.app.service.impl.AppServiceInstanceServiceImpl.isMiddleware;
import static io.choerodon.devops.infra.constant.MarketConstant.APP_SHELVES_CODE;
import static io.choerodon.devops.infra.constant.MarketConstant.APP_SHELVES_NAME;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.type.TypeReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import io.choerodon.core.domain.Page;
import io.choerodon.core.exception.CommonException;
import io.choerodon.core.oauth.DetailsHelper;
import io.choerodon.devops.api.vo.*;
import io.choerodon.devops.api.vo.market.MarketServiceDeployObjectVO;
import io.choerodon.devops.api.vo.market.MarketServiceVO;
import io.choerodon.devops.app.service.*;
import io.choerodon.devops.infra.constant.MiscConstants;
import io.choerodon.devops.infra.constant.ResourceCheckConstant;
import io.choerodon.devops.infra.dto.*;
import io.choerodon.devops.infra.enums.*;
import io.choerodon.devops.infra.enums.deploy.RdupmTypeEnum;
import io.choerodon.devops.infra.feign.operator.BaseServiceClientOperator;
import io.choerodon.devops.infra.feign.operator.MarketServiceClientOperator;
import io.choerodon.devops.infra.handler.ClusterConnectionHandler;
import io.choerodon.devops.infra.mapper.AppServiceInstanceMapper;
import io.choerodon.devops.infra.mapper.DevopsDeployAppCenterEnvMapper;
import io.choerodon.devops.infra.mapper.DevopsEnvironmentMapper;
import io.choerodon.devops.infra.util.*;
import io.choerodon.mybatis.pagehelper.PageHelper;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;

/**
 * @Author: shanyu
 * @DateTime: 2021-08-18 15:28
 **/
@Service
public class DevopsDeployAppCenterServiceImpl implements DevopsDeployAppCenterService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DevopsDeployAppCenterServiceImpl.class);

    private static final String POD_RUNNING_STATUS = "Running";

    @Autowired
    DevopsDeployAppCenterEnvMapper devopsDeployAppCenterEnvMapper;
    @Autowired
    DevopsEnvironmentMapper devopsEnvironmentMapper;
    @Autowired
    private DevopsDeployAppCenterEnvMapper appCenterEnvMapper;
    @Autowired
    @Lazy
    private AppServiceInstanceService instanceService;
    @Autowired
    private AppServiceService appServiceService;
    @Autowired
    private DevopsEnvironmentService environmentService;
    @Autowired
    private BaseServiceClientOperator baseServiceClientOperator;
    @Autowired
    private MarketServiceClientOperator marketServiceClientOperator;
    @Autowired
    private DevopsEnvResourceService devopsEnvResourceService;
    @Autowired
    private DevopsEnvPodService devopsEnvPodService;
    @Autowired
    private AppServiceInstanceService appServiceInstanceService;
    @Autowired
    private DevopsServiceService devopsServiceService;
    @Autowired
    private AppServiceInstanceMapper appServiceInstanceMapper;
    @Autowired
    private ClusterConnectionHandler clusterConnectionHandler;
    @Autowired
    private DevopsEnvCommandService devopsEnvCommandService;
    @Autowired
    private DevopsDeploymentService devopsDeploymentService;
    @Autowired
    private PermissionHelper permissionHelper;
    @Autowired
    private AppExceptionRecordService appExceptionRecordService;
    @Autowired
    private DevopsCiJobService devopsCiJobService;
    @Autowired
    private DevopsEnvResourceDetailService devopsEnvResourceDetailService;
    @Autowired
    private PipelineService pipelineService;

    @Override
    public Boolean checkNameUnique(Long envId, String rdupmType, Long objectId, String name) {
        return devopsDeployAppCenterEnvMapper.checkNameUnique(rdupmType, objectId, envId, name);
    }

    @Override
    public Boolean checkCodeUnique(Long envId, String rdupmType, Long objectId, String code) {
        return devopsDeployAppCenterEnvMapper.checkCodeUnique(rdupmType, objectId, envId, code);
    }

    @Override
    public void checkNameUniqueAndThrow(Long envId, String rdupmType, Long objectId, String name) {
        if (Boolean.FALSE.equals(checkNameUnique(envId, rdupmType, objectId, name))) {
            throw new CommonException("devops.env.app.center.name.exist");
        }
    }

    @Override
    public void checkCodeUniqueAndThrow(Long envId, String rdupmType, Long objectId, String name) {
        if (Boolean.FALSE.equals(checkCodeUnique(envId, rdupmType, objectId, name))) {
            throw new CommonException("devops.env.app.center.code.exist");
        }
    }

    @Override
    public void checkNameAndCodeUniqueAndThrow(Long envId, String rdupmType, Long objectId, String name, String code) {
        if (!StringUtils.isEmpty(name)) {
            checkNameUniqueAndThrow(envId, rdupmType, objectId, name);
        }

        if (!StringUtils.isEmpty(code)) {
            checkCodeUniqueAndThrow(envId, rdupmType, objectId, code);
        }
    }

    @Override
    public Page<DevopsDeployAppCenterVO> listApp(Long projectId, Long envId, String name, String rdupmType, String operationType, String params, PageRequest pageable) {
        Page<DevopsDeployAppCenterVO> devopsDeployAppCenterVOS;
        long userId = DetailsHelper.getUserDetails().getUserId();
        if (permissionHelper.isGitlabProjectOwnerOrGitlabAdmin(projectId, userId)) {
            devopsDeployAppCenterVOS = PageHelper.doPageAndSort(pageable, () -> devopsDeployAppCenterEnvMapper.listAppFromEnv(projectId, envId, name, rdupmType, operationType, params));
        } else {
            devopsDeployAppCenterVOS = PageHelper.doPageAndSort(pageable, () -> devopsDeployAppCenterEnvMapper.listAppFromEnvByUserId(projectId, envId, name, rdupmType, operationType, params, userId));
        }
        List<DevopsDeployAppCenterVO> devopsDeployAppCenterVOList = devopsDeployAppCenterVOS.getContent();
        if (CollectionUtils.isEmpty(devopsDeployAppCenterVOList)) {
            return devopsDeployAppCenterVOS;
        }
        Map<Long, DevopsEnvironmentDTO> devopsEnvironmentDTOMap = combineDevopsEnvironmentDTOMap(devopsDeployAppCenterVOList);
        List<Long> upgradeClusterList = clusterConnectionHandler.getUpdatedClusterList();
        //将查询出的AppServiceInstanceInfoDTO集合组合成以id为key，实体为value的map
        Map<Long, AppServiceInstanceInfoDTO> appServiceInstanceInfoDTOMap = devopsInstanceDTOMap(devopsDeployAppCenterVOList);
        devopsDeployAppCenterVOList.forEach(devopsDeployAppCenterVO -> {
            DevopsEnvironmentDTO devopsEnvironmentDTO = devopsEnvironmentDTOMap.get(devopsDeployAppCenterVO.getEnvId());
            if (!ObjectUtils.isEmpty(devopsEnvironmentDTO)) {
                devopsDeployAppCenterVO.setEnvName(devopsEnvironmentDTO.getName());
                devopsDeployAppCenterVO.setEnvActive(devopsEnvironmentDTO.getActive());
            }
            devopsDeployAppCenterVO.setEnvConnected(upgradeClusterList.contains(devopsEnvironmentDTO.getClusterId()));
            AppCenterEnvDetailVO detailVO = new AppCenterEnvDetailVO();
            List<DevopsEnvPodDTO> devopsEnvPodDTOS = new ArrayList<>();
            AppServiceInstanceInfoDTO appServiceInstanceInfoDTO = appServiceInstanceInfoDTOMap.get(devopsDeployAppCenterVO.getObjectId());
            if (RdupmTypeEnum.CHART.value().equals(devopsDeployAppCenterVO.getRdupmType())) {
                if (!ObjectUtils.isEmpty(appServiceInstanceInfoDTO)) {
                    //将AppInstanceInfo里的值赋值给AppCenter
                    setAppInstanceInfoToAppCenter(projectId, devopsDeployAppCenterVO, appServiceInstanceInfoDTO);
                }
                // 添加pod运行统计
                devopsEnvPodDTOS = devopsEnvPodService.baseListByInstanceId(devopsDeployAppCenterVO.getObjectId());
            } else if (RdupmTypeEnum.DEPLOYMENT.value().equals(devopsDeployAppCenterVO.getRdupmType())) {
                // 添加pod运行统计
                devopsEnvPodDTOS = devopsEnvPodService.listPodByKind(devopsDeployAppCenterVO.getEnvId(), ResourceType.DEPLOYMENT.getType(), devopsDeployAppCenterVO.getCode());
                DevopsDeploymentVO deploymentVO = devopsDeploymentService.selectByPrimaryWithCommandInfo(devopsDeployAppCenterVO.getObjectId());
                if (!ObjectUtils.isEmpty(deploymentVO)) {
                    if (CommandStatus.OPERATING.getStatus().equals(deploymentVO.getCommandStatus())) {
                        devopsDeployAppCenterVO.setStatus(deploymentVO.getCommandStatus());
                    } else {
                        devopsDeployAppCenterVO.setStatus(deploymentVO.getStatus());
                    }
                    devopsDeployAppCenterVO.setError(deploymentVO.getError());
                }
            }
            calculatePodStatus(devopsEnvPodDTOS, detailVO);
            if (!ObjectUtils.isEmpty(detailVO)) {
                devopsDeployAppCenterVO.setPodCount(detailVO.getPodCount());
                devopsDeployAppCenterVO.setPodRunningCount(detailVO.getPodRunningCount());
            }
        });
        UserDTOFillUtil.fillUserInfo(devopsDeployAppCenterVOList, "createdBy", "creator");
        return devopsDeployAppCenterVOS;
    }

    @Override
    public AppCenterEnvDetailVO envAppDetail(Long projectId, Long appCenterId) {
        DevopsDeployAppCenterEnvDTO centerEnvDTO = appCenterEnvMapper.selectByPrimaryKey(appCenterId);
        if (centerEnvDTO == null) {
            return new AppCenterEnvDetailVO();
        }
        AppCenterEnvDetailVO detailVO = ConvertUtils.convertObject(centerEnvDTO, AppCenterEnvDetailVO.class);
        detailVO.setId(appCenterId);
        detailVO.setDeployWay(AppCenterDeployWayEnum.CONTAINER.getValue());
        detailVO.setRdupmType(centerEnvDTO.getRdupmType());
        detailVO.setInstanceId(centerEnvDTO.getObjectId());
        if (centerEnvDTO.getRdupmType().equals(RdupmTypeEnum.CHART.value())) {
            AppServiceInstanceInfoDTO appServiceInstanceInfoDTO = appServiceInstanceMapper.queryInfoById(centerEnvDTO.getObjectId());
            detailVO.setObjectStatus(appServiceInstanceInfoDTO.getStatus());
            BeanUtils.copyProperties(appServiceInstanceInfoDTO, detailVO, "id");
            detailVO.setLastUpdateDate(appServiceInstanceInfoDTO.getLastUpdateDate());
            detailVO.setUpdater(baseServiceClientOperator.queryUserByUserId(appServiceInstanceInfoDTO.getLastUpdatedBy() == 0L ? centerEnvDTO.getLastUpdatedBy() : appServiceInstanceInfoDTO.getLastUpdatedBy()));
            if (centerEnvDTO.getChartSource().equals(AppSourceType.NORMAL.getValue()) ||
                    centerEnvDTO.getChartSource().equals(AppSourceType.SHARE.getValue())) {
                AppServiceDTO appServiceDTO = appServiceService.baseQuery(appServiceInstanceInfoDTO.getAppServiceId());
                detailVO.setAppServiceCode(appServiceDTO.getCode());
                detailVO.setAppServiceName(appServiceDTO.getName());
            } else {
                MarketServiceVO marketServiceVO = marketServiceClientOperator.queryMarketService(projectId, appServiceInstanceInfoDTO.getAppServiceId());
                // 处理已经下架的应用 不存在的情况
                if (marketServiceVO == null) {
                    marketServiceVO = new MarketServiceVO();
                    marketServiceVO.setMarketServiceCode(APP_SHELVES_CODE);
                    marketServiceVO.setMarketServiceName(APP_SHELVES_NAME);
                }
                // 这里的code仅hzero开放平台的应用有数据，其他应用从marketServiceDeployObjectVO获取
                detailVO.setAppServiceCode(marketServiceVO.getMarketServiceCode());
                detailVO.setAppServiceName(marketServiceVO.getMarketServiceName());

                Set<Long> deployObjectIds = new HashSet<>();
                deployObjectIds.add(appServiceInstanceInfoDTO.getCommandVersionId());
                // 这个id可能为空
                if (appServiceInstanceInfoDTO.getEffectCommandVersionId() != null) {
                    deployObjectIds.add(appServiceInstanceInfoDTO.getEffectCommandVersionId());
                }
                Map<Long, MarketServiceDeployObjectVO> versions = marketServiceClientOperator.listDeployObjectsByIds(appServiceInstanceInfoDTO.getProjectId(), deployObjectIds).stream().collect(Collectors.toMap(MarketServiceDeployObjectVO::getId, Function.identity()));
                MarketServiceDeployObjectVO marketServiceDeployObjectVO = versions.get(appServiceInstanceInfoDTO.getCommandVersionId());
                if (marketServiceDeployObjectVO != null) {
                    if (StringUtils.isEmpty(marketServiceVO.getMarketServiceCode()) && !StringUtils.isEmpty(marketServiceDeployObjectVO.getDevopsAppServiceCode())) {
                        detailVO.setAppServiceCode(marketServiceDeployObjectVO.getDevopsAppServiceCode());
                    }
                    detailVO.setMktAppVersionId(versions.get(appServiceInstanceInfoDTO.getCommandVersionId()).getMarketAppVersionId());
                    detailVO.setMktDeployObjectId(appServiceInstanceInfoDTO.getCommandVersionId());
                    // 如果是中间件，直接以应用版本作为生效版本
                    if (isMiddleware(appServiceInstanceInfoDTO.getSource())) {
                        detailVO.setCommandVersion(versions.get(appServiceInstanceInfoDTO.getCommandVersionId()).getMarketServiceVersion());
                    } else {
                        detailVO.setCommandVersion(versions.get(appServiceInstanceInfoDTO.getCommandVersionId()).getDevopsAppServiceVersion());
                    }
                    detailVO.setCurrentVersionAvailable(true);
                } else {
                    detailVO.setCommandVersion("版本已被删除");
                    detailVO.setCurrentVersionAvailable(false);
                }

                if (versions.get(appServiceInstanceInfoDTO.getEffectCommandVersionId()) != null) {
                    // 如果是中间件，直接以应用版本作为生效版本
                    if (isMiddleware(appServiceInstanceInfoDTO.getSource())) {
                        detailVO.setEffectCommandVersion(versions.get(appServiceInstanceInfoDTO.getEffectCommandVersionId()).getMarketServiceVersion());
                    } else {
                        detailVO.setEffectCommandVersion(versions.get(appServiceInstanceInfoDTO.getEffectCommandVersionId()).getDevopsAppServiceVersion());
                    }
                }

                List<MarketServiceDeployObjectVO> upgradeAble = marketServiceClientOperator.queryUpgradeDeployObjects(appServiceInstanceInfoDTO.getProjectId(), appServiceInstanceInfoDTO.getAppServiceId(), appServiceInstanceInfoDTO.getCommandVersionId());
                // 这里查出的版本是包含当前的版本和最新的版本，两个版本
                // 如果只查出一个版本，但不是当前版本，就是可升级的
                if (upgradeAble.size() > 1) {
                    detailVO.setUpgradeAvailable(true);
                } else {
                    detailVO.setUpgradeAvailable(upgradeAble.size() == 1 && !appServiceInstanceInfoDTO.getCommandVersionId().equals(upgradeAble.get(0).getId()));
                }
            }
            // 添加pod运行统计
            List<DevopsEnvPodDTO> devopsEnvPodDTOS = devopsEnvPodService.baseListByInstanceId(centerEnvDTO.getObjectId());
            calculatePodStatus(devopsEnvPodDTOS, detailVO);

            // 计算是否存在关联的网络
            detailVO.setExistService(devopsServiceService.countInstanceService(projectId, centerEnvDTO.getEnvId(), centerEnvDTO.getObjectId()) > 0);

        } else if (centerEnvDTO.getRdupmType().equals(RdupmTypeEnum.DEPLOYMENT.value())) {
            // 添加pod运行统计
            List<DevopsEnvPodDTO> devopsEnvPodDTOS = devopsEnvPodService.listPodByKind(centerEnvDTO.getEnvId(), ResourceType.DEPLOYMENT.getType(), centerEnvDTO.getCode());
            calculatePodStatus(devopsEnvPodDTOS, detailVO);
            // 查询最近的成功状态的envCommandId作为deployment的effectCommandId
            Long effectCommandId = devopsEnvCommandService.queryWorkloadEffectCommandId(ObjectType.DEPLOYMENT.getType(), centerEnvDTO.getObjectId());
            detailVO.setEffectCommandId(effectCommandId);

            // 设置deployment的状态、appConfig、containerConfig
            DevopsDeploymentDTO devopsDeploymentDTO = devopsDeploymentService.selectByPrimaryKey(centerEnvDTO.getObjectId());
            detailVO.setObjectStatus(devopsDeploymentDTO.getStatus());
            detailVO.setAppConfig(JsonHelper.unmarshalByJackson(devopsDeploymentDTO.getAppConfig(), DevopsDeployGroupAppConfigVO.class));
            detailVO.setContainerConfig(JsonHelper.unmarshalByJackson(devopsDeploymentDTO.getContainerConfig(), new TypeReference<List<DevopsDeployGroupContainerConfigVO>>() {
            }));
            detailVO.setLastUpdateDate(devopsDeploymentDTO.getLastUpdateDate());
            detailVO.setUpdater(baseServiceClientOperator.queryUserByUserId(devopsDeploymentDTO.getLastUpdatedBy() == 0L ? centerEnvDTO.getLastUpdatedBy() : devopsDeploymentDTO.getLastUpdatedBy()));
        }
        // 环境信息查询
        DevopsEnvironmentDTO environmentDTO = environmentService.baseQueryById(centerEnvDTO.getEnvId());
        detailVO.setEnvironmentId(centerEnvDTO.getEnvId());
        detailVO.setEnvCode(environmentDTO.getCode());
        detailVO.setEnvName(environmentDTO.getName());
        List<Long> upgradeClusterList = clusterConnectionHandler.getUpdatedClusterList();
        detailVO.setEnvActive(environmentDTO.getActive());
        detailVO.setEnvConnected(upgradeClusterList.contains(environmentDTO.getClusterId()));

        detailVO.setAppCode(detailVO.getCode());
        detailVO.setAppName(detailVO.getName());
        detailVO.setAppId(detailVO.getId());

        detailVO.setCreator(baseServiceClientOperator.queryUserByUserId(centerEnvDTO.getCreatedBy()));
        detailVO.setChartSource(centerEnvDTO.getChartSource());
        return detailVO;
    }

    @Override
    public void calculatePodStatus(List<DevopsEnvPodDTO> devopsEnvPodDTOS, AppCenterEnvDetailVO detailVO) {
        if (CollectionUtils.isEmpty(devopsEnvPodDTOS)) {
            detailVO.setPodRunningCount(devopsEnvPodDTOS.size());
        } else {
            detailVO.setPodRunningCount((int) devopsEnvPodDTOS.stream().filter(v -> Boolean.TRUE.equals(v.getReady()) && POD_RUNNING_STATUS.equals(v.getStatus())).count());
        }
        detailVO.setPodCount(devopsEnvPodDTOS.size());
    }

    @Override
    public List<InstanceEventVO> envAppEvent(Long projectId, Long appCenterId) {
        DevopsDeployAppCenterEnvDTO centerEnvDTO = appCenterEnvMapper.selectByPrimaryKey(appCenterId);
        if (centerEnvDTO == null) {
            return Collections.EMPTY_LIST;
        }
        if (centerEnvDTO.getRdupmType().equals(RdupmTypeEnum.CHART.value())) {
            return devopsEnvResourceService.listInstancePodEvent(centerEnvDTO.getObjectId());
        } else {
            return devopsEnvResourceService.listDeploymentPodEvent(centerEnvDTO.getObjectId());
        }
    }

    @Override
    public Page<DevopsEnvPodVO> envAppPodsPage(Long projectId, Long appCenterId, PageRequest pageRequest, String searchParam) {
        DevopsDeployAppCenterEnvDTO centerEnvDTO = appCenterEnvMapper.selectByPrimaryKey(appCenterId);
        if (centerEnvDTO.getRdupmType().equals(RdupmTypeEnum.CHART.value())) {
            AppServiceInstanceDTO instanceDTO = instanceService.baseQuery(centerEnvDTO.getObjectId());
            return devopsEnvPodService.pageByOptions(
                    projectId, instanceDTO.getEnvId(), instanceDTO.getAppServiceId(), centerEnvDTO.getObjectId(), pageRequest, searchParam);
        } else {
            return devopsEnvPodService.pageByKind(projectId, centerEnvDTO.getEnvId(), ResourceType.DEPLOYMENT.getType(), centerEnvDTO.getCode(), pageRequest, searchParam);
        }
    }

    @Override
    public DevopsEnvResourceVO envAppRelease(Long projectId, Long appCenterId) {
        DevopsDeployAppCenterEnvDTO centerEnvDTO = appCenterEnvMapper.selectByPrimaryKey(appCenterId);
        if (centerEnvDTO == null) {
            return new DevopsEnvResourceVO();
        }
        if (centerEnvDTO.getRdupmType().equals(RdupmTypeEnum.CHART.value())) {
            return appServiceInstanceService.listResourcesInHelmRelease(centerEnvDTO.getObjectId());
        } else {
            return devopsEnvResourceService.listResourcesByDeploymentId(centerEnvDTO.getObjectId());
        }
    }

    @Override
    public Page<DevopsServiceVO> envChartService(Long projectId, Long appCenterId, PageRequest pageRequest, String searchParam) {
        DevopsDeployAppCenterEnvDTO centerEnvDTO = appCenterEnvMapper.selectByPrimaryKey(appCenterId);
        if (centerEnvDTO == null) {
            return new Page<>();
        }
        if (centerEnvDTO.getRdupmType().equals(RdupmTypeEnum.CHART.value())) {
            AppServiceInstanceDTO instanceDTO = instanceService.baseQuery(centerEnvDTO.getObjectId());
            return devopsServiceService.pageByInstance(projectId, instanceDTO.getEnvId(), centerEnvDTO.getObjectId(), pageRequest, instanceDTO.getAppServiceId(), searchParam);
        }
        return new Page<>();
    }

    @Transactional
    @Override
    public void baseCreate(DevopsDeployAppCenterEnvDTO devopsDeployAppCenterEnvDTO) {
        MapperUtil.resultJudgedInsertSelective(devopsDeployAppCenterEnvMapper, devopsDeployAppCenterEnvDTO, "devops.env.app.center.insert");
    }

    @Transactional
    @Override
    public void baseUpdate(DevopsDeployAppCenterEnvDTO devopsDeployAppCenterEnvDTO) {
        MapperUtil.resultJudgedUpdateByPrimaryKeySelective(devopsDeployAppCenterEnvMapper, devopsDeployAppCenterEnvDTO, "devops.env.app.center.update");
    }

    @Override
    @Transactional
    public DevopsDeployAppCenterEnvDTO baseCreate(String name, String code, Long projectId, Long objectId, Long envId, String operationType, String chartSource, String rdupmType) {
        DevopsDeployAppCenterEnvDTO devopsDeployAppCenterEnvDTO = new DevopsDeployAppCenterEnvDTO();
        devopsDeployAppCenterEnvDTO.setName(name);
        devopsDeployAppCenterEnvDTO.setCode(code);
        devopsDeployAppCenterEnvDTO.setProjectId(projectId);
        devopsDeployAppCenterEnvDTO.setObjectId(objectId);
        devopsDeployAppCenterEnvDTO.setEnvId(envId);
        devopsDeployAppCenterEnvDTO.setOperationType(operationType);
        devopsDeployAppCenterEnvDTO.setChartSource(chartSource);
        devopsDeployAppCenterEnvDTO.setRdupmType(rdupmType);
        baseCreate(devopsDeployAppCenterEnvDTO);
        return devopsDeployAppCenterEnvDTO;
    }

    @Override
    public DevopsDeployAppCenterEnvDTO queryByEnvIdAndCode(Long environmentId, String appCode) {
        return devopsDeployAppCenterEnvMapper.queryByEnvIdAndCode(environmentId, appCode);
    }

    @Override
    public DevopsDeployAppCenterEnvDTO queryByRdupmTypeAndObjectId(RdupmTypeEnum rdupmTypeEnum, Long objectId) {
        DevopsDeployAppCenterEnvDTO devopsDeployAppCenterEnvDTO = new DevopsDeployAppCenterEnvDTO();
        devopsDeployAppCenterEnvDTO.setRdupmType(rdupmTypeEnum.value());
        devopsDeployAppCenterEnvDTO.setObjectId(objectId);
        return devopsDeployAppCenterEnvMapper.selectOne(devopsDeployAppCenterEnvDTO);
    }

    @Override
    @Transactional
    public void deleteByEnvIdAndObjectIdAndRdupmType(Long envId, Long objectId, String rdupmType) {
        devopsDeployAppCenterEnvMapper.deleteByEnvIdAndObjectIdAndRdupmType(envId, objectId, rdupmType);
    }

    @Override
    public DevopsDeployAppCenterEnvDTO selectByPrimaryKey(Long id) {
        return devopsDeployAppCenterEnvMapper.selectByPrimaryKey(id);
    }

    @Override
    public Page<DevopsDeployAppCenterVO> pageByProjectIdAndEnvId(Long projectId, Long envId, PageRequest pageRequest) {
        return PageHelper.doPageAndSort(pageRequest, () -> devopsDeployAppCenterEnvMapper.listByProjectIdAndEnvId(projectId, envId));
    }

    @Override
    public Page<DevopsDeployAppCenterVO> pageByProjectIdAndEnvIdAndAppId(Long projectId, Long envId, Long appServiceId, PageRequest pageRequest) {
        return PageHelper.doPageAndSort(pageRequest, () -> devopsDeployAppCenterEnvMapper.listByProjectIdAndEnvIdAndAppId(projectId, envId, appServiceId));
    }

    @Override
    public Page<DevopsDeployAppCenterVO> pageChart(Long projectId, Long envId, String name, String operationType, String params, PageRequest pageable) {
        //分页查询满足的DevopsDeployAppCenterVO
        Page<DevopsDeployAppCenterVO> devopsDeployAppCenterVOS = pageAppCenterByUserId(projectId, envId, name, operationType, params, pageable);
        List<DevopsDeployAppCenterVO> devopsDeployAppCenterVOList = devopsDeployAppCenterVOS.getContent();
        if (CollectionUtils.isEmpty(devopsDeployAppCenterVOList)) {
            return devopsDeployAppCenterVOS;
        }
        //将查询出的DevopsEnvironmentDTO集合组合成以id为key，实体为value的map
        Map<Long, DevopsEnvironmentDTO> devopsEnvironmentDTOMap = combineDevopsEnvironmentDTOMap(devopsDeployAppCenterVOList);
        List<Long> upgradeClusterList = clusterConnectionHandler.getUpdatedClusterList();
        //将查询出的AppServiceInstanceInfoDTO集合组合成以id为key，实体为value的map
        Map<Long, AppServiceInstanceInfoDTO> appServiceInstanceInfoDTOMap = devopsInstanceDTOMap(devopsDeployAppCenterVOList);
        //将查询出的MarketServiceDeployObjectVO集合组合成以id为key，实体为value的map
        Map<Long, MarketServiceDeployObjectVO> devopsMarketDTOMap = devopsMarketDTOMap(projectId, devopsDeployAppCenterVOList, appServiceInstanceInfoDTOMap);
        devopsDeployAppCenterVOList.forEach(devopsDeployAppCenterVO -> {
            //将环境信息赋值给AppCenterVO
            setEnvInfoToAppCenterVO(devopsDeployAppCenterVO, devopsEnvironmentDTOMap.get(devopsDeployAppCenterVO.getEnvId()), upgradeClusterList);
            AppServiceInstanceInfoDTO appServiceInstanceInfoDTO = appServiceInstanceInfoDTOMap.get(devopsDeployAppCenterVO.getObjectId());
            if (!ObjectUtils.isEmpty(appServiceInstanceInfoDTO)) {
                //将AppInstanceInfo里的值赋值给AppCenter
                setAppInstanceInfoToAppCenter(projectId, devopsDeployAppCenterVO, appServiceInstanceInfoDTO);
            }
            // 添加pod运行统计
            setPodInfoToAppCenter(devopsDeployAppCenterVO);
            MarketServiceDeployObjectVO marketServiceDeployObjectVO = devopsMarketDTOMap.get(appServiceInstanceInfoDTO.getAppServiceVersionId());
            if (isMarketOrMiddleware(devopsDeployAppCenterVO.getChartSource()) && !ObjectUtils.isEmpty(marketServiceDeployObjectVO)) {
                //将版本，appName等内容赋值给AppCenterVO
                setVersionAndAppNameToAppCenterVO(devopsDeployAppCenterVO, marketServiceDeployObjectVO.getMarketServiceName(), marketServiceDeployObjectVO.getMarketAppVersionId(), marketServiceDeployObjectVO.getMarketServiceVersion());
            }
        });
        //赋值创建者信息
        UserDTOFillUtil.fillUserInfo(devopsDeployAppCenterVOList, "createdBy", "creator");
        return devopsDeployAppCenterVOS;
    }

    @Override
    public List<PipelineInstanceReferenceVO> queryPipelineReference(Long projectId, Long appId) {
        DevopsDeployAppCenterEnvDTO devopsDeployAppCenterEnvDTO = selectByPrimaryKey(appId);
        List<PipelineInstanceReferenceVO> pipelineInstanceReferenceVOList = new ArrayList<>();
        if (RdupmTypeEnum.DEPLOYMENT.value().equals(devopsDeployAppCenterEnvDTO.getRdupmType())) {
            pipelineInstanceReferenceVOList.add(devopsCiJobService.queryPipelineReferenceEnvApp(projectId, appId));
        } else {
            PipelineInstanceReferenceVO pipelineInstanceReferenceVO = devopsCiJobService.queryChartPipelineReference(projectId, appId);
            if (pipelineInstanceReferenceVO != null) {
                pipelineInstanceReferenceVOList.add(pipelineInstanceReferenceVO);
            }
            pipelineInstanceReferenceVOList.addAll(pipelineService.listAppPipelineReference(projectId, appId));
        }

        return pipelineInstanceReferenceVOList;

    }

    @Override
    public void checkEnableDeleteAndThrowE(Long projectId, RdupmTypeEnum rdupmTypeEnum, Long instanceId) {
        DevopsDeployAppCenterEnvDTO devopsDeployAppCenterEnvDTO = queryByRdupmTypeAndObjectId(rdupmTypeEnum, instanceId);
        if (devopsDeployAppCenterEnvDTO == null) {
            return;
        }
        if (devopsCiJobService.queryChartPipelineReference(projectId, devopsDeployAppCenterEnvDTO.getId()) != null) {
            throw new CommonException(ResourceCheckConstant.DEVOPS_APP_INSTANCE_IS_ASSOCIATED_WITH_PIPELINE);
        }
    }

    @Override
    public List<DevopsDeployAppCenterVO> listByAppServiceIds(Long envId, Set<Long> appServiceIds) {
        if (CollectionUtils.isEmpty(appServiceIds)) {
            return new ArrayList<>();
        }
        return devopsDeployAppCenterEnvMapper.listByAppServiceIds(envId, appServiceIds);
    }

    @Override
    @Transactional
    public void enableMetric(Long projectId, Long appId) {
        DevopsDeployAppCenterEnvDTO devopsDeployAppCenterEnvDTO = devopsDeployAppCenterEnvMapper.selectByPrimaryKey(appId);
        CommonExAssertUtil.assertTrue(projectId.equals(devopsDeployAppCenterEnvDTO.getProjectId()), MiscConstants.DEVOPS_OPERATING_RESOURCE_IN_OTHER_PROJECT);

        devopsDeployAppCenterEnvDTO.setMetricDeployStatus(true);
        devopsDeployAppCenterEnvMapper.updateByPrimaryKeySelective(devopsDeployAppCenterEnvDTO);

        // 开启应用监控时，同步一次应用状态
        AppServiceInstanceDTO appServiceInstanceDTO = appServiceInstanceService.baseQuery(devopsDeployAppCenterEnvDTO.getObjectId());
        List<DevopsEnvResourceDTO> devopsEnvResourceDTOS = devopsEnvResourceService.baseListByInstanceId(devopsDeployAppCenterEnvDTO.getObjectId());
        if (!CollectionUtils.isEmpty(devopsEnvResourceDTOS)) {
            for (DevopsEnvResourceDTO devopsEnvResourceDTO : devopsEnvResourceDTOS) {
                if (ResourceType.STATEFULSET.getType().equals(devopsEnvResourceDTO.getKind())
                        || ResourceType.DEPLOYMENT.getType().equals(devopsEnvResourceDTO.getKind())) {
                    DevopsEnvResourceDetailDTO devopsEnvResourceDetailDTO = devopsEnvResourceDetailService.baseQueryByResourceDetailId(devopsEnvResourceDTO.getResourceDetailId());
                    appExceptionRecordService.createOrUpdateExceptionRecord(devopsEnvResourceDTO.getKind(), devopsEnvResourceDetailDTO.getMessage(), appServiceInstanceDTO);
                }
            }
        }
    }

    @Override
    @Transactional
    public void disableMetric(Long projectId, Long appId) {
        DevopsDeployAppCenterEnvDTO devopsDeployAppCenterEnvDTO = devopsDeployAppCenterEnvMapper.selectByPrimaryKey(appId);
        CommonExAssertUtil.assertTrue(projectId.equals(devopsDeployAppCenterEnvDTO.getProjectId()), MiscConstants.DEVOPS_OPERATING_RESOURCE_IN_OTHER_PROJECT);

        devopsDeployAppCenterEnvDTO.setMetricDeployStatus(false);
        devopsDeployAppCenterEnvMapper.updateByPrimaryKeySelective(devopsDeployAppCenterEnvDTO);

        // 如果关闭时，还存在未终止的异常记录。则手动终止
        appExceptionRecordService.completeExceptionRecord(appId);
    }

    @Override
    public ExceptionTimesVO queryExceptionTimesChartInfo(Long projectId, Long appId, Date startTime, Date endTime) {
        List<AppExceptionRecordDTO> appExceptionRecordDTOS = appExceptionRecordService.listByAppIdAndDate(appId, startTime, endTime);

        // 按日期分组
        Map<String, List<AppExceptionRecordDTO>> listMap = appExceptionRecordDTOS.stream().collect(Collectors.groupingBy(v -> new java.sql.Date(v.getStartDate().getTime()).toString()));
        List<String> dateList = new ArrayList<>();
        List<Long> exceptionTimesList = new ArrayList<>();
        List<Long> downTimeList = new ArrayList<>();

        LocalDate localDate = startTime.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate endDate = endTime.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        while (!localDate.isAfter(endDate)) {
            List<AppExceptionRecordDTO> appExceptionRecordsOfDay = listMap.get(localDate.toString());
            dateList.add(localDate.toString());
            // 没有异常点则填0
            if (CollectionUtils.isEmpty(appExceptionRecordsOfDay)) {
                downTimeList.add(0L);
                exceptionTimesList.add(0L);
            } else {
                // 存在异常点则计算
                downTimeList.add(appExceptionRecordsOfDay.stream().filter(r -> Boolean.TRUE.equals(r.getDowntime())).count());
                exceptionTimesList.add(appExceptionRecordsOfDay.stream().filter(r -> Boolean.FALSE.equals(r.getDowntime())).count());
            }
            localDate = localDate.plusDays(1);
        }

        // 统计次数
        Long exceptionTotalTimes = 0L;
        Long downTimeTotalTimes = 0L;
        for (Long aLong : downTimeList) {
            downTimeTotalTimes += aLong;
        }
        for (Long aLong : exceptionTimesList) {
            exceptionTotalTimes += aLong;
        }

        return new ExceptionTimesVO(exceptionTotalTimes, downTimeTotalTimes, dateList, exceptionTimesList, downTimeList);
    }

    @Override
    public ExceptionDurationVO queryExceptionDurationChartInfo(Long projectId, Long appId, Date startTime, Date endTime) {
        List<AppExceptionRecordDTO> appExceptionRecordDTOS = appExceptionRecordService.listCompletedByAppIdAndDate(appId, startTime, endTime);

        List<ExceptionRecordVO> exceptionDurationList = new ArrayList<>();
        List<ExceptionRecordVO> downTimeDurationList = new ArrayList<>();
        List<String> dateList = new ArrayList<>();


        for (AppExceptionRecordDTO r : appExceptionRecordDTOS) {
            long duration = (r.getEndDate().getTime() - r.getStartDate().getTime()) / 1000;
            double durationMinute = duration / 60.0;
            ExceptionRecordVO exceptionRecordVO = new ExceptionRecordVO(duration, String.format("%.2f", durationMinute), r.getStartDate(), r.getEndDate(), r.getStartDate());
            if (Boolean.TRUE.equals(r.getDowntime())) {
                downTimeDurationList.add(exceptionRecordVO);
            } else {
                exceptionDurationList.add(exceptionRecordVO);
            }
        }

        LocalDate localDate = startTime.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate endDate = endTime.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        while (!localDate.isAfter(endDate)) {
            dateList.add(localDate.toString());
            localDate = localDate.plusDays(1);
        }
        // 统计时长
        Long exceptionTotalDuration = 0L;
        Long downTimeTotalDuration = 0L;
        for (ExceptionRecordVO exceptionRecordVO : downTimeDurationList) {
            downTimeTotalDuration += exceptionRecordVO.getDuration();
        }
        for (ExceptionRecordVO exceptionRecordVO : exceptionDurationList) {
            exceptionTotalDuration += exceptionRecordVO.getDuration();
        }
        return new ExceptionDurationVO(exceptionTotalDuration, downTimeTotalDuration, dateList, exceptionDurationList, downTimeDurationList);
    }

    @Transactional
    @Override
    public void delete(DevopsDeployAppCenterEnvDTO devopsDeployAppCenterEnvDTO) {
        devopsDeployAppCenterEnvMapper.delete(devopsDeployAppCenterEnvDTO);
    }

    private Page<DevopsDeployAppCenterVO> pageAppCenterByUserId(Long projectId, Long envId, String name, String operationType, String params, PageRequest pageable) {
        Page<DevopsDeployAppCenterVO> devopsDeployAppCenterVOS;
        long userId = DetailsHelper.getUserDetails().getUserId();
        if (permissionHelper.isGitlabProjectOwnerOrGitlabAdmin(projectId, userId)) {
            devopsDeployAppCenterVOS = PageHelper.doPageAndSort(pageable, () -> devopsDeployAppCenterEnvMapper.listChart(projectId, envId, name, operationType, params));
        } else {
            devopsDeployAppCenterVOS = PageHelper.doPageAndSort(pageable, () -> devopsDeployAppCenterEnvMapper.listChartByUserId(projectId, envId, name, operationType, params, userId));
        }
        return devopsDeployAppCenterVOS;
    }

    @Override
    public void setAppInstanceInfoToAppCenter(Long projectId, DevopsDeployAppCenterVO devopsDeployAppCenterVO, AppServiceInstanceInfoDTO appServiceInstanceInfoDTO) {
        devopsDeployAppCenterVO.setAppServiceId(appServiceInstanceInfoDTO.getAppServiceId());
        devopsDeployAppCenterVO.setStatus(appServiceInstanceInfoDTO.getStatus());
        devopsDeployAppCenterVO.setError(appServiceInstanceInfoDTO.getError());
        if (isMarketOrMiddleware(appServiceInstanceInfoDTO.getSource())) {
            devopsDeployAppCenterVO.setUpgradeAvailable(isUpgradeAvailable(projectId, appServiceInstanceInfoDTO));
        } else {
            setVersionAndAppNameToAppCenterVO(devopsDeployAppCenterVO, appServiceInstanceInfoDTO.getAppServiceName(), appServiceInstanceInfoDTO.getCommandVersionId(), appServiceInstanceInfoDTO.getCommandVersion());
        }
    }

    private void setPodInfoToAppCenter(DevopsDeployAppCenterVO devopsDeployAppCenterVO) {
        AppCenterEnvDetailVO detailVO = new AppCenterEnvDetailVO();
        List<DevopsEnvPodDTO> devopsEnvPodDTOS = devopsEnvPodService.baseListByInstanceId(devopsDeployAppCenterVO.getObjectId());
        calculatePodStatus(devopsEnvPodDTOS, detailVO);
        if (!ObjectUtils.isEmpty(detailVO)) {
            devopsDeployAppCenterVO.setPodCount(detailVO.getPodCount());
            devopsDeployAppCenterVO.setPodRunningCount(detailVO.getPodRunningCount());
        }
    }

    @Override
    public Map<Long, DevopsEnvironmentDTO> combineDevopsEnvironmentDTOMap(List<DevopsDeployAppCenterVO> devopsDeployAppCenterVOList) {
        List<DevopsEnvironmentDTO> environmentDTOS = environmentService.baseListByIds(devopsDeployAppCenterVOList.stream().map(DevopsDeployAppCenterVO::getEnvId).collect(Collectors.toList()));
        return environmentDTOS.stream().collect(Collectors.toMap(DevopsEnvironmentDTO::getId, Function.identity()));
    }

    private boolean isMarketOrMiddleware(String source) {
        return AppServiceInstanceServiceImpl.isMarket(source) || AppServiceInstanceServiceImpl.isMiddleware(source);
    }

    private Map<Long, MarketServiceDeployObjectVO> devopsMarketDTOMap(Long projectId, List<DevopsDeployAppCenterVO> devopsDeployAppCenterVOList, Map<Long, AppServiceInstanceInfoDTO> appServiceInstanceInfoDTOMap) {
        Map<Long, MarketServiceDeployObjectVO> devopsMarketDTOMap = new HashMap<>();
        Set<Long> marketObjectIds = devopsDeployAppCenterVOList.stream().filter(appCenterVO -> isMarketOrMiddleware(appCenterVO.getChartSource())).map(appCenterVO ->
                appServiceInstanceInfoDTOMap.get(appCenterVO.getObjectId()).getAppServiceVersionId()).collect(Collectors.toSet());
        if (!CollectionUtils.isEmpty(marketObjectIds)) {
            List<MarketServiceDeployObjectVO> marketServiceDeployObjectVOList = marketServiceClientOperator.listDeployObjectsByIds(projectId, marketObjectIds);
            if (!CollectionUtils.isEmpty(marketServiceDeployObjectVOList)) {
                devopsMarketDTOMap = marketServiceDeployObjectVOList.stream().collect(Collectors.toMap(MarketServiceDeployObjectVO::getId, Function.identity()));
            }
        }
        return devopsMarketDTOMap;
    }

    @Override
    public Map<Long, AppServiceInstanceInfoDTO> devopsInstanceDTOMap(List<DevopsDeployAppCenterVO> devopsDeployAppCenterVOList) {
        List<Long> instanceIds = devopsDeployAppCenterVOList.stream().map(DevopsDeployAppCenterVO::getObjectId).collect(Collectors.toList());
        List<AppServiceInstanceInfoDTO> appServiceInstanceInfoDTOList = appServiceInstanceMapper.listInfoById(instanceIds);
        Map<Long, AppServiceInstanceInfoDTO> devopsMarketDTOMap = new HashMap<>();
        if (!CollectionUtils.isEmpty(appServiceInstanceInfoDTOList)) {
            devopsMarketDTOMap = appServiceInstanceInfoDTOList.stream().collect(Collectors.toMap(AppServiceInstanceInfoDTO::getId, Function.identity()));
        }
        return devopsMarketDTOMap;
    }

    private void setVersionAndAppNameToAppCenterVO(DevopsDeployAppCenterVO devopsDeployAppCenterVO, String name, Long versionId, String version) {
        devopsDeployAppCenterVO.setAppServiceName(name);
        devopsDeployAppCenterVO.setCommandVersionId(versionId);
        devopsDeployAppCenterVO.setCommandVersion(version);
    }

    private void setEnvInfoToAppCenterVO(DevopsDeployAppCenterVO devopsDeployAppCenterVO, DevopsEnvironmentDTO devopsEnvironmentDTO, List<Long> upgradeClusterList) {
        if (!ObjectUtils.isEmpty(devopsEnvironmentDTO)) {
            devopsDeployAppCenterVO.setEnvName(devopsEnvironmentDTO.getName());
            devopsDeployAppCenterVO.setEnvActive(devopsEnvironmentDTO.getActive());
            devopsDeployAppCenterVO.setEnvConnected(upgradeClusterList.contains(devopsEnvironmentDTO.getClusterId()));
        }
    }

    private boolean isUpgradeAvailable(Long projectId, AppServiceInstanceInfoDTO appServiceInstanceinfoDTO) {
        List<MarketServiceDeployObjectVO> upgradeAble = marketServiceClientOperator.queryUpgradeDeployObjects(projectId, appServiceInstanceinfoDTO.getAppServiceId(), appServiceInstanceinfoDTO.getCommandVersionId());
        // 这里查出的版本是包含当前的版本和最新的版本，两个版本
        // 如果只查出一个版本，但不是当前版本，就是可升级的
        boolean upgradeAvailable;
        if (upgradeAble.size() > 1) {
            upgradeAvailable = true;
        } else {
            upgradeAvailable = upgradeAble.size() == 1 && !appServiceInstanceinfoDTO.getCommandVersionId().equals(upgradeAble.get(0).getId());
        }
        return upgradeAvailable;
    }

    @Override
    public Integer batchInsert(List<DevopsDeployAppCenterEnvDTO> devopsDeployAppCenterEnvDTOList) {
        return devopsDeployAppCenterEnvMapper.batchInsert(devopsDeployAppCenterEnvDTOList);
    }
}
