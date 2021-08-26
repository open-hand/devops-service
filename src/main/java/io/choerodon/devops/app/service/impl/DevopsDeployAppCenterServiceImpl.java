package io.choerodon.devops.app.service.impl;

import static io.choerodon.devops.app.service.impl.AppServiceInstanceServiceImpl.isMiddleware;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import io.choerodon.core.domain.Page;
import io.choerodon.devops.api.vo.*;
import io.choerodon.devops.api.vo.market.MarketServiceDeployObjectVO;
import io.choerodon.devops.api.vo.market.MarketServiceVO;
import io.choerodon.devops.app.service.*;
import io.choerodon.devops.infra.dto.*;
import io.choerodon.devops.infra.enums.AppCenterDeployWayEnum;
import io.choerodon.devops.infra.enums.AppSourceType;
import io.choerodon.devops.infra.enums.ResourceType;
import io.choerodon.devops.infra.enums.deploy.RdupmTypeEnum;
import io.choerodon.devops.infra.feign.operator.BaseServiceClientOperator;
import io.choerodon.devops.infra.feign.operator.MarketServiceClientOperator;
import io.choerodon.devops.infra.handler.ClusterConnectionHandler;
import io.choerodon.devops.infra.mapper.AppServiceInstanceMapper;
import io.choerodon.devops.infra.mapper.DevopsDeployAppCenterEnvMapper;
import io.choerodon.devops.infra.mapper.DevopsDeployAppCenterHostMapper;
import io.choerodon.devops.infra.mapper.DevopsEnvironmentMapper;
import io.choerodon.devops.infra.util.ConvertUtils;
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

    @Override
    public Page<DevopsDeployAppCenterVO> listApp(Long projectId, Long envId, String name, String rdupmType, String operationType, String params, PageRequest pageable) {
        Page<DevopsDeployAppCenterVO> devopsDeployAppCenterVOS = PageHelper.doPageAndSort(pageable, () -> devopsDeployAppCenterEnvMapper.listAppFromEnv(projectId, envId, name, rdupmType, operationType, params));
        List<DevopsDeployAppCenterVO> devopsDeployAppCenterVOList = devopsDeployAppCenterVOS.getContent();
        if (CollectionUtils.isEmpty(devopsDeployAppCenterVOList)) {
            return devopsDeployAppCenterVOS;
        }
        List<DevopsEnvironmentDTO> environmentDTOS = environmentService.batchQueryByIds(devopsDeployAppCenterVOList.stream().map(DevopsDeployAppCenterVO::getEnvId).collect(Collectors.toList()));
        Map<Long, DevopsEnvironmentDTO> devopsEnvironmentDTOMap = environmentDTOS.stream().collect(Collectors.toMap(DevopsEnvironmentDTO::getId, Function.identity()));
        List<Long> upgradeClusterList = clusterConnectionHandler.getUpdatedClusterList();
        devopsDeployAppCenterVOList.forEach(devopsDeployAppCenterVO -> {
            DevopsEnvironmentDTO devopsEnvironmentDTO = devopsEnvironmentDTOMap.get(devopsDeployAppCenterVO.getEnvId());
            devopsDeployAppCenterVO.setEnvName(devopsEnvironmentDTO.getName());
            devopsDeployAppCenterVO.setEnvActive(devopsEnvironmentDTO.getActive());
            devopsDeployAppCenterVO.setEnvConnected(upgradeClusterList.contains(devopsEnvironmentDTO.getClusterId()));
            devopsDeployAppCenterVO.setStatus(appServiceInstanceService.queryInstanceStatusByEnvIdAndCode(devopsDeployAppCenterVO.getCode(), devopsDeployAppCenterVO.getEnvId()));
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
        if (centerEnvDTO.getRdupmType().equals(RdupmTypeEnum.CHART.value())) {
            AppServiceInstanceInfoDTO appServiceInstanceInfoDTO = appServiceInstanceMapper.queryInfoById(centerEnvDTO.getObjectId());
            detailVO.setObjectStatus(appServiceInstanceInfoDTO.getStatus());
            BeanUtils.copyProperties(appServiceInstanceInfoDTO, detailVO);
            detailVO.setInstanceId(centerEnvDTO.getObjectId());
            if (centerEnvDTO.getChartSource().equals(AppSourceType.NORMAL.getValue()) ||
                    centerEnvDTO.getChartSource().equals(AppSourceType.SHARE.getValue())) {
                AppServiceDTO appServiceDTO = appServiceService.baseQuery(appServiceInstanceInfoDTO.getAppServiceId());
                detailVO.setAppServiceCode(appServiceDTO.getCode());
                detailVO.setAppServiceName(appServiceDTO.getName());
            } else {
                MarketServiceVO marketServiceVO = marketServiceClientOperator.queryMarketService(projectId, appServiceInstanceInfoDTO.getAppServiceId());
                detailVO.setAppServiceCode(marketServiceVO.getMarketServiceCode());
                detailVO.setAppServiceName(marketServiceVO.getMarketServiceName());

                Set<Long> deployObjectIds = new HashSet<>();
                deployObjectIds.add(appServiceInstanceInfoDTO.getCommandVersionId());
                // 这个id可能为空
                if (appServiceInstanceInfoDTO.getEffectCommandVersionId() != null) {
                    deployObjectIds.add(appServiceInstanceInfoDTO.getEffectCommandVersionId());
                }
                Map<Long, MarketServiceDeployObjectVO> versions = marketServiceClientOperator.listDeployObjectsByIds(appServiceInstanceInfoDTO.getProjectId(), deployObjectIds).stream().collect(Collectors.toMap(MarketServiceDeployObjectVO::getId, Function.identity()));
                if (versions.get(appServiceInstanceInfoDTO.getCommandVersionId()) != null) {
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
        } else if (centerEnvDTO.getRdupmType().equals(RdupmTypeEnum.DEPLOYMENT.value())){
            // 添加pod运行统计
            List<DevopsEnvPodDTO> devopsEnvPodDTOS = devopsEnvPodService.listPodByKind(centerEnvDTO.getEnvId(), ResourceType.DEPLOYMENT.getType(), centerEnvDTO.getCode());
            calculatePodStatus(devopsEnvPodDTOS, detailVO);

        }
        // 环境信息查询
        DevopsEnvironmentDTO environmentDTO = environmentService.baseQueryById(centerEnvDTO.getEnvId());
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
    @Transactional
    public void deleteByEnvIdAndObjectIdAndRdupmType(Long envId, Long objectId, String rdupmType) {
        devopsDeployAppCenterEnvMapper.deleteByEnvIdAndObjectIdAndRdupmType(envId, objectId, rdupmType);
    }
}
