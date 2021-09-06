package io.choerodon.devops.app.service.impl;

import static io.choerodon.devops.app.service.impl.AppServiceInstanceServiceImpl.isMiddleware;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.type.TypeReference;
import org.hzero.mybatis.BatchInsertHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
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
import io.choerodon.devops.infra.dto.*;
import io.choerodon.devops.infra.enums.AppCenterDeployWayEnum;
import io.choerodon.devops.infra.enums.AppSourceType;
import io.choerodon.devops.infra.enums.ObjectType;
import io.choerodon.devops.infra.enums.ResourceType;
import io.choerodon.devops.infra.enums.deploy.OperationTypeEnum;
import io.choerodon.devops.infra.enums.deploy.RdupmTypeEnum;
import io.choerodon.devops.infra.feign.operator.BaseServiceClientOperator;
import io.choerodon.devops.infra.feign.operator.MarketServiceClientOperator;
import io.choerodon.devops.infra.handler.ClusterConnectionHandler;
import io.choerodon.devops.infra.mapper.AppServiceInstanceMapper;
import io.choerodon.devops.infra.mapper.DevopsDeployAppCenterEnvMapper;
import io.choerodon.devops.infra.mapper.DevopsDeployAppCenterHostMapper;
import io.choerodon.devops.infra.mapper.DevopsEnvironmentMapper;
import io.choerodon.devops.infra.util.ConvertUtils;
import io.choerodon.devops.infra.util.JsonHelper;
import io.choerodon.devops.infra.util.MapperUtil;
import io.choerodon.devops.infra.util.UserDTOFillUtil;
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
    private DevopsDeployAppCenterHostMapper devopsDeployAppCenterHostMapper;
    @Autowired
    private ClusterConnectionHandler clusterConnectionHandler;
    @Autowired
    private DevopsEnvCommandService devopsEnvCommandService;
    @Autowired
    private DevopsDeploymentService devopsDeploymentService;
    @Autowired
    private PermissionHelper permissionHelper;
    @Autowired
    @Qualifier("devopsAppCenterHelper")
    private BatchInsertHelper<DevopsDeployAppCenterEnvDTO> batchInsertHelper;

    @Override
    public Boolean checkNameUnique(Long projectId, Long envId, String rdupmType, Long objectId, String name) {
        return devopsDeployAppCenterEnvMapper.checkNameUnique(rdupmType, objectId, envId, name);
    }

    @Override
    public Boolean checkCodeUnique(Long projectId, Long envId, String rdupmType, Long objectId, String code) {
        return devopsDeployAppCenterEnvMapper.checkCodeUnique(rdupmType, objectId, envId, code);
    }

    @Override
    public void checkNameAndCodeUnique(Long projectId, Long envId, String rdupmType, Long objectId, String name, String code) {
        if (!checkNameUnique(projectId, envId, rdupmType, objectId, name)) {
            throw new CommonException("error.env.app.center.name.exist");
        }

        if (!checkCodeUnique(projectId, envId, rdupmType, objectId, code)) {
            throw new CommonException("error.env.app.center.code.exist");
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
        List<DevopsEnvironmentDTO> environmentDTOS = environmentService.baseListByIds(devopsDeployAppCenterVOList.stream().map(DevopsDeployAppCenterVO::getEnvId).collect(Collectors.toList()));
        Map<Long, DevopsEnvironmentDTO> devopsEnvironmentDTOMap = environmentDTOS.stream().collect(Collectors.toMap(DevopsEnvironmentDTO::getId, Function.identity()));
        List<Long> upgradeClusterList = clusterConnectionHandler.getUpdatedClusterList();
        devopsDeployAppCenterVOList.forEach(devopsDeployAppCenterVO -> {
            DevopsEnvironmentDTO devopsEnvironmentDTO = devopsEnvironmentDTOMap.get(devopsDeployAppCenterVO.getEnvId());
            if (!ObjectUtils.isEmpty(devopsEnvironmentDTO)) {
                devopsDeployAppCenterVO.setEnvName(devopsEnvironmentDTO.getName());
                devopsDeployAppCenterVO.setEnvActive(devopsEnvironmentDTO.getActive());
            }
            devopsDeployAppCenterVO.setEnvConnected(upgradeClusterList.contains(devopsEnvironmentDTO.getClusterId()));
            AppCenterEnvDetailVO detailVO = new AppCenterEnvDetailVO();
            List<DevopsEnvPodDTO> devopsEnvPodDTOS = new ArrayList<>();
            if (RdupmTypeEnum.CHART.value().equals(devopsDeployAppCenterVO.getRdupmType())) {
                // 添加pod运行统计
                devopsEnvPodDTOS = devopsEnvPodService.baseListByInstanceId(devopsDeployAppCenterVO.getObjectId());

                devopsDeployAppCenterVO.setStatus(appServiceInstanceService.queryInstanceStatusByEnvIdAndCode(devopsDeployAppCenterVO.getCode(), devopsDeployAppCenterVO.getEnvId()));
            } else if (RdupmTypeEnum.DEPLOYMENT.value().equals(devopsDeployAppCenterVO.getRdupmType())) {
                // 添加pod运行统计
                devopsEnvPodDTOS = devopsEnvPodService.listPodByKind(devopsDeployAppCenterVO.getEnvId(), ResourceType.DEPLOYMENT.getType(), devopsDeployAppCenterVO.getCode());
                DevopsDeploymentDTO deploymentDTO = devopsDeploymentService.selectByPrimaryKey(devopsDeployAppCenterVO.getObjectId());
                if (!ObjectUtils.isEmpty(deploymentDTO)) {
                    devopsDeployAppCenterVO.setStatus(deploymentDTO.getStatus());
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
        AppCenterEnvDetailVO detailVO = ConvertUtils.convertObject(centerEnvDTO, AppCenterEnvDetailVO.class);
        detailVO.setAppCenterId(appCenterId);
        detailVO.setDeployWay(AppCenterDeployWayEnum.CONTAINER.getValue());
        detailVO.setRdupmType(centerEnvDTO.getRdupmType());
        detailVO.setInstanceId(centerEnvDTO.getObjectId());
        if (centerEnvDTO.getRdupmType().equals(RdupmTypeEnum.CHART.value())) {
            AppServiceInstanceInfoDTO appServiceInstanceInfoDTO = appServiceInstanceMapper.queryInfoById(centerEnvDTO.getObjectId());
            detailVO.setObjectStatus(appServiceInstanceInfoDTO.getStatus());
            BeanUtils.copyProperties(appServiceInstanceInfoDTO, detailVO);
            if (centerEnvDTO.getChartSource().equals(AppSourceType.NORMAL.getValue()) ||
                    centerEnvDTO.getChartSource().equals(AppSourceType.SHARE.getValue())) {
                AppServiceDTO appServiceDTO = appServiceService.baseQuery(appServiceInstanceInfoDTO.getAppServiceId());
                detailVO.setAppServiceCode(appServiceDTO.getCode());
                detailVO.setAppServiceName(appServiceDTO.getName());
            } else {
                MarketServiceVO marketServiceVO = marketServiceClientOperator.queryMarketService(projectId, appServiceInstanceInfoDTO.getAppServiceId());
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
                    if (!StringUtils.isEmpty(marketServiceVO.getMarketServiceCode()) && !StringUtils.isEmpty(marketServiceDeployObjectVO.getDevopsAppServiceCode())) {
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
                } else {
                    detailVO.setCommandVersion("版本已被删除");
                }
            }
            // 添加pod运行统计
            List<DevopsEnvPodDTO> devopsEnvPodDTOS = devopsEnvPodService.baseListByInstanceId(centerEnvDTO.getObjectId());
            calculatePodStatus(devopsEnvPodDTOS, detailVO);
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
        }
        // 环境信息查询
        DevopsEnvironmentDTO environmentDTO = environmentService.baseQueryById(centerEnvDTO.getEnvId());
        detailVO.setEnvironmentId(centerEnvDTO.getEnvId());
        detailVO.setEnvCode(environmentDTO.getCode());
        detailVO.setEnvName(environmentDTO.getName());
        List<Long> upgradeClusterList = clusterConnectionHandler.getUpdatedClusterList();
        detailVO.setEnvActive(environmentDTO.getActive());
        detailVO.setEnvConnected(upgradeClusterList.contains(environmentDTO.getClusterId()));

        detailVO.setCreator(baseServiceClientOperator.queryUserByUserId(centerEnvDTO.getCreatedBy()));
        detailVO.setChartSource(centerEnvDTO.getChartSource());
        return detailVO;
    }

    private void calculatePodStatus(List<DevopsEnvPodDTO> devopsEnvPodDTOS, AppCenterEnvDetailVO detailVO) {
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
        if (centerEnvDTO.getRdupmType().equals(RdupmTypeEnum.CHART.value())) {
            return appServiceInstanceService.listResourcesInHelmRelease(centerEnvDTO.getObjectId());
        } else {
            return devopsEnvResourceService.listResourcesByDeploymentId(centerEnvDTO.getObjectId());
        }
    }

    @Override
    public Page<DevopsServiceVO> envChartService(Long projectId, Long appCenterId, PageRequest pageRequest, String searchParam) {
        DevopsDeployAppCenterEnvDTO centerEnvDTO = appCenterEnvMapper.selectByPrimaryKey(appCenterId);
        if (centerEnvDTO.getRdupmType().equals(RdupmTypeEnum.CHART.value())) {
            AppServiceInstanceDTO instanceDTO = instanceService.baseQuery(centerEnvDTO.getObjectId());
            return devopsServiceService.pageByInstance(projectId, instanceDTO.getEnvId(), centerEnvDTO.getObjectId(), pageRequest, instanceDTO.getAppServiceId(), searchParam);
        }
        return new Page<>();
    }

    @Transactional
    @Override
    public void baseCreate(DevopsDeployAppCenterEnvDTO devopsDeployAppCenterEnvDTO) {
        MapperUtil.resultJudgedInsertSelective(devopsDeployAppCenterEnvMapper, devopsDeployAppCenterEnvDTO, "error.env.app.center.insert");
    }

    @Transactional
    @Override
    public void baseUpdate(DevopsDeployAppCenterEnvDTO devopsDeployAppCenterEnvDTO) {
        MapperUtil.resultJudgedUpdateByPrimaryKeySelective(devopsDeployAppCenterEnvMapper, devopsDeployAppCenterEnvDTO, "error.env.app.center.update");
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
    public void baseHostCreate(DevopsDeployAppCenterHostDTO devopsDeployAppCenterHostDTO) {
        MapperUtil.resultJudgedInsertSelective(devopsDeployAppCenterHostMapper, devopsDeployAppCenterHostDTO, "error.host.app.center.insert");
    }

    @Override
    public void baseHostCreate(String name, String code, Long projectId, Long objectId, Long hostId, String operationType, String jarSource, String rdupmType) {
        DevopsDeployAppCenterHostDTO devopsDeployAppCenterHostDTO = new DevopsDeployAppCenterHostDTO();
        devopsDeployAppCenterHostDTO.setName(name);
        devopsDeployAppCenterHostDTO.setCode(code);
        devopsDeployAppCenterHostDTO.setProjectId(projectId);
        devopsDeployAppCenterHostDTO.setObjectId(objectId);
        devopsDeployAppCenterHostDTO.setHostId(hostId);
        devopsDeployAppCenterHostDTO.setOperationType(operationType);
        devopsDeployAppCenterHostDTO.setJarSource(jarSource);
        devopsDeployAppCenterHostDTO.setRdupmType(rdupmType);
        baseHostCreate(devopsDeployAppCenterHostDTO);
    }

    @Override
    public void fixData() {
        int totalCount = appServiceInstanceService.countInstance();
        int pageNumber = 0;
        int pageSize = 100;
        int totalPage = (totalCount + pageSize - 1) / pageSize;
        LOGGER.info("start to fix DevopsDeployAppCenterEnv data.");
        do {
            LOGGER.info("=====DevopsDeployAppCenterEnv================={}/{}=================", pageNumber, totalPage - 1);
            PageRequest pageRequest = new PageRequest();
            pageRequest.setPage(pageNumber);
            pageRequest.setSize(pageSize);
            Page<AppServiceInstanceDTO> result = PageHelper.doPage(pageRequest, () -> appServiceInstanceService.listInstances());
            if (!CollectionUtils.isEmpty(result.getContent())) {
                List<DevopsDeployAppCenterEnvDTO> devopsDeployAppCenterEnvDTOList = result.getContent().stream().map(i -> {
                    DevopsDeployAppCenterEnvDTO devopsDeployAppCenterEnvDTO = new DevopsDeployAppCenterEnvDTO();
                    devopsDeployAppCenterEnvDTO.setName(i.getCode());
                    devopsDeployAppCenterEnvDTO.setCode(i.getCode());
                    devopsDeployAppCenterEnvDTO.setProjectId(i.getProjectId());
                    devopsDeployAppCenterEnvDTO.setEnvId(i.getEnvId());
                    devopsDeployAppCenterEnvDTO.setObjectId(i.getId());
                    devopsDeployAppCenterEnvDTO.setRdupmType(RdupmTypeEnum.CHART.value());
                    devopsDeployAppCenterEnvDTO.setOperationType(AppSourceType.MIDDLEWARE.getValue().equals(i.getSource()) ? OperationTypeEnum.BASE_COMPONENT.value() : OperationTypeEnum.CREATE_APP.value());

                    // 如果是normal，需要具体判断本项目还是共享应用
                    if (AppSourceType.NORMAL.getValue().equals(i.getSource())) {
                        AppServiceDTO appServiceDTO = appServiceService.baseQuery(i.getAppServiceId());
                        if (appServiceDTO == null) {
                            // 该实例对应的应用服务信息不存在
                            return null;
                        }
                        if (appServiceDTO.getProjectId().equals(i.getProjectId())) {
                            devopsDeployAppCenterEnvDTO.setChartSource(AppSourceType.NORMAL.getValue());
                        } else {
                            devopsDeployAppCenterEnvDTO.setChartSource(AppSourceType.SHARE.getValue());
                        }
                    } else {
                        devopsDeployAppCenterEnvDTO.setChartSource(i.getSource());
                    }
                    return devopsDeployAppCenterEnvDTO;
                }).filter(Objects::nonNull)
                        .collect(Collectors.toList());
                batchInsertHelper.batchInsert(devopsDeployAppCenterEnvDTOList);
            }
            pageNumber++;
        } while (pageNumber < totalPage);
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
    public List<AppCenterEnvDetailVO> listByProjectIdAndEnvId(Long projectId, Long envId, String rdupmType) {
        return devopsDeployAppCenterEnvMapper.listByProjectIdAndEnvId(projectId, envId, rdupmType);
    }
}
