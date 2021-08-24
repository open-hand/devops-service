package io.choerodon.devops.app.service.impl;

import java.util.List;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import io.choerodon.core.domain.Page;
import io.choerodon.devops.api.vo.*;
import io.choerodon.devops.api.vo.market.MarketServiceVO;
import io.choerodon.devops.app.service.*;
import io.choerodon.devops.infra.dto.*;
import io.choerodon.devops.infra.enums.AppCenterChartSourceEnum;
import io.choerodon.devops.infra.enums.AppCenterDeployWayEnum;
import io.choerodon.devops.infra.enums.AppCenterRdupmTypeEnum;
import io.choerodon.devops.infra.enums.ResourceType;
import io.choerodon.devops.infra.feign.operator.BaseServiceClientOperator;
import io.choerodon.devops.infra.feign.operator.MarketServiceClientOperator;
import io.choerodon.devops.infra.handler.ClusterConnectionHandler;
import io.choerodon.devops.infra.mapper.AppServiceInstanceMapper;
import io.choerodon.devops.infra.mapper.DevopsDeployAppCenterEnvMapper;
import io.choerodon.devops.infra.mapper.DevopsEnvironmentMapper;
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

    @Override
    public Page<DevopsDeployAppCenterVO> listApp(Long projectId, Long envId, String name, String rdupmType, String operationType, String params, PageRequest pageable) {
        Page<DevopsDeployAppCenterVO> devopsDeployAppCenterVOS = PageHelper.doPageAndSort(pageable, () -> devopsDeployAppCenterEnvMapper.listAppFromEnv(projectId, envId, name, rdupmType, operationType, params));
        List<DevopsDeployAppCenterVO> devopsDeployAppCenterVOList = devopsDeployAppCenterVOS.getContent();
        if (CollectionUtils.isEmpty(devopsDeployAppCenterVOList)) {
            return devopsDeployAppCenterVOS;
        }
        devopsDeployAppCenterVOList.forEach(devopsDeployAppCenterVO -> {
            DevopsEnvironmentDTO devopsEnvAppServiceDTO = new DevopsEnvironmentDTO();
            devopsEnvAppServiceDTO.setId(devopsDeployAppCenterVO.getEnvId());
            devopsDeployAppCenterVO.setEnvName(devopsEnvironmentMapper.selectByPrimaryKey(devopsEnvAppServiceDTO).getName());
            devopsDeployAppCenterVO.setStatus(appServiceInstanceService.queryInstanceStatusByEnvIdAndCode(devopsDeployAppCenterVO.getCode(), devopsDeployAppCenterVO.getEnvId()));
        });
        UserDTOFillUtil.fillUserInfo(devopsDeployAppCenterVOList, "createdBy", "creator");
        return devopsDeployAppCenterVOS;
    }

    @Override
    public AppCenterEnvDetailVO envAppDetail(Long projectId, Long appCenterId) {
        AppCenterEnvDetailVO detailVO = new AppCenterEnvDetailVO();
        DevopsDeployAppCenterEnvDTO centerEnvDTO = appCenterEnvMapper.selectByPrimaryKey(appCenterId);
        BeanUtils.copyProperties(centerEnvDTO, detailVO);
        detailVO.setAppCenterId(appCenterId);
        detailVO.setDeployWay(AppCenterDeployWayEnum.CONTAINER.getValue());
        detailVO.setRdupmType(centerEnvDTO.getRdupmType());
        if (centerEnvDTO.getRdupmType().equals(AppCenterRdupmTypeEnum.CHART.getType())) {
            AppServiceInstanceInfoDTO appServiceInstanceInfoDTO = appServiceInstanceMapper.queryInfoById(centerEnvDTO.getObjectId());
            detailVO.setObjectStatus(appServiceInstanceInfoDTO.getStatus());
            BeanUtils.copyProperties(appServiceInstanceInfoDTO, detailVO);
            detailVO.setInstanceId(centerEnvDTO.getObjectId());
            if (centerEnvDTO.getChartSource().equals(AppCenterChartSourceEnum.NORMAL.getValue()) ||
                    centerEnvDTO.getChartSource().equals(AppCenterChartSourceEnum.SHARE.getValue())) {
                AppServiceDTO appServiceDTO = appServiceService.baseQuery(appServiceInstanceInfoDTO.getAppServiceId());
                detailVO.setAppServiceCode(appServiceDTO.getCode());
                detailVO.setAppServiceName(appServiceDTO.getName());
            } else {
                MarketServiceVO marketServiceVO = marketServiceClientOperator.queryMarketService(projectId, appServiceInstanceInfoDTO.getAppServiceId());
                detailVO.setAppServiceCode(marketServiceVO.getMarketServiceCode());
                detailVO.setAppServiceName(marketServiceVO.getMarketServiceName());
            }
        }
        // 环境信息查询
        DevopsEnvironmentDTO environmentDTO = environmentService.baseQueryById(centerEnvDTO.getEnvId());
        detailVO.setEnvCode(environmentDTO.getCode());
        detailVO.setEnvName(environmentDTO.getName());
        List<Long> upgradeClusterList = clusterConnectionHandler.getUpdatedClusterList();
        detailVO.setEnvActive(environmentDTO.getActive());
        detailVO.setEnvConnected(upgradeClusterList.contains(environmentDTO.getClusterId()));

        detailVO.setIamUserDTO(baseServiceClientOperator.queryUserByUserId(centerEnvDTO.getCreatedBy()));
        detailVO.setChartSource(centerEnvDTO.getChartSource());
        return detailVO;
    }

    @Override
    public List<InstanceEventVO> envAppEvent(Long projectId, Long appCenterId) {
        DevopsDeployAppCenterEnvDTO centerEnvDTO = appCenterEnvMapper.selectByPrimaryKey(appCenterId);
        if (centerEnvDTO.getRdupmType().equals(AppCenterRdupmTypeEnum.CHART.getType())) {
            return devopsEnvResourceService.listInstancePodEvent(centerEnvDTO.getObjectId());
        } else {
            // 部署组应用事件查询 todo scp
        }
        return null;
    }

    @Override
    public Page<DevopsEnvPodVO> envAppPodsPage(Long projectId, Long appCenterId, PageRequest pageRequest, String searchParam) {
        DevopsDeployAppCenterEnvDTO centerEnvDTO = appCenterEnvMapper.selectByPrimaryKey(appCenterId);
        if (centerEnvDTO.getRdupmType().equals(AppCenterRdupmTypeEnum.CHART.getType())) {
            AppServiceInstanceDTO instanceDTO = instanceService.baseQuery(centerEnvDTO.getObjectId());
            return devopsEnvPodService.pageByOptions(
                    projectId, instanceDTO.getEnvId(), instanceDTO.getAppServiceId(), centerEnvDTO.getObjectId(), pageRequest, searchParam);
        } else {
            devopsEnvPodService.pageByKind(projectId, centerEnvDTO.getEnvId(), ResourceType.DEPLOYMENT.getType(), centerEnvDTO.getCode(), pageRequest, searchParam);
        }
        return null;
    }

    @Override
    public DevopsEnvResourceVO envAppRelease(Long projectId, Long appCenterId) {
        DevopsDeployAppCenterEnvDTO centerEnvDTO = appCenterEnvMapper.selectByPrimaryKey(appCenterId);
        if (centerEnvDTO.getRdupmType().equals(AppCenterRdupmTypeEnum.CHART.getType())) {
            return appServiceInstanceService.listResourcesInHelmRelease(centerEnvDTO.getObjectId());
        } else {
            return devopsEnvResourceService.listResourcesByDeploymentId(centerEnvDTO.getObjectId());
        }
    }

    @Override
    public Page<DevopsServiceVO> envChartService(Long projectId, Long appCenterId, PageRequest pageRequest, String searchParam) {
        DevopsDeployAppCenterEnvDTO centerEnvDTO = appCenterEnvMapper.selectByPrimaryKey(appCenterId);
        if (centerEnvDTO.getRdupmType().equals(AppCenterRdupmTypeEnum.CHART.getType())) {
            AppServiceInstanceDTO instanceDTO = instanceService.baseQuery(centerEnvDTO.getObjectId());
            return devopsServiceService.pageByInstance(projectId, instanceDTO.getEnvId(), centerEnvDTO.getObjectId(), pageRequest, instanceDTO.getAppServiceId(), searchParam);
        }
        return null;
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
    public DevopsDeployAppCenterEnvDTO queryByEnvIdAndCode(Long environmentId, String appCode) {
        return devopsDeployAppCenterEnvMapper.queryByEnvIdAndCode(environmentId, appCode);
    }
}
