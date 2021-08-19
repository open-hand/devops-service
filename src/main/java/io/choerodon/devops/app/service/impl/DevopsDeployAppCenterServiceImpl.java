package io.choerodon.devops.app.service.impl;

import java.util.List;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import io.choerodon.core.domain.Page;
import io.choerodon.devops.api.vo.*;
import io.choerodon.devops.api.vo.market.MarketServiceVO;
import io.choerodon.devops.app.service.*;
import io.choerodon.devops.infra.dto.AppServiceDTO;
import io.choerodon.devops.infra.dto.AppServiceInstanceDTO;
import io.choerodon.devops.infra.dto.DevopsDeployAppCenterEnvDTO;
import io.choerodon.devops.infra.dto.DevopsEnvironmentDTO;
import io.choerodon.devops.infra.dto.iam.IamUserDTO;
import io.choerodon.devops.infra.enums.AppCenterChartSourceEnum;
import io.choerodon.devops.infra.enums.AppCenterDeployWayEnum;
import io.choerodon.devops.infra.enums.AppCenterRdupmTypeEnum;
import io.choerodon.devops.infra.feign.operator.BaseServiceClientOperator;
import io.choerodon.devops.infra.feign.operator.MarketServiceClientOperator;
import io.choerodon.devops.infra.mapper.DevopsDeployAppCenterEnvMapper;
import io.choerodon.devops.infra.mapper.DevopsEnvironmentMapper;
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

    @Override
    public Page<DevopsDeployAppCenterVO> listApp(Long projectId, Long envId, String name, String rdupmType, String operationType, PageRequest pageable) {
        Page<DevopsDeployAppCenterVO> devopsDeployAppCenterVOS = PageHelper.doPageAndSort(pageable, () -> devopsDeployAppCenterEnvMapper.listAppFromEnv(projectId, envId, name, rdupmType, operationType));
        List<DevopsDeployAppCenterVO> devopsDeployAppCenterVOList = devopsDeployAppCenterVOS.getContent();
        if (CollectionUtils.isEmpty(devopsDeployAppCenterVOList)) {
            return devopsDeployAppCenterVOS;
        }
        devopsDeployAppCenterVOList.forEach(devopsDeployAppCenterVO -> {
            DevopsEnvironmentDTO devopsEnvAppServiceDTO = new DevopsEnvironmentDTO();
            devopsEnvAppServiceDTO.setId(devopsDeployAppCenterVO.getEnvId());
            devopsDeployAppCenterVO.setEnvName(devopsEnvironmentMapper.selectByPrimaryKey(devopsEnvAppServiceDTO).getName());
        });
        UserDTOFillUtil.fillUserInfo(devopsDeployAppCenterVOList, "createdBy", "iamUserDTO");
        return devopsDeployAppCenterVOS;
    }

    @Override
    public AppCenterEnvDetailVO envAppDetail(Long projectId, Long appCenterId) {
        AppCenterEnvDetailVO detailVO = new AppCenterEnvDetailVO();
        DevopsDeployAppCenterEnvDTO centerEnvDTO = appCenterEnvMapper.selectByPrimaryKey(appCenterId);
        BeanUtils.copyProperties(centerEnvDTO, detailVO);
        detailVO.setAppCenterId(appCenterId);
        detailVO.setDeployWay(AppCenterDeployWayEnum.CONTAINER.getValue());
        if (centerEnvDTO.getRdupmType().equals(AppCenterRdupmTypeEnum.CHART.getType())) {
            AppServiceInstanceDTO instanceDTO = instanceService.baseQuery(centerEnvDTO.getObjectId());
            detailVO.setObjectStatus(instanceDTO.getStatus());
            if (centerEnvDTO.getChartSource().equals(AppCenterChartSourceEnum.NORMAL.getValue()) ||
                    centerEnvDTO.getChartSource().equals(AppCenterChartSourceEnum.SHARE.getValue())) {
                AppServiceDTO appServiceDTO = appServiceService.baseQuery(instanceDTO.getAppServiceId());
                detailVO.setAppServiceCode(appServiceDTO.getCode());
                detailVO.setAppServiceName(appServiceDTO.getName());
            } else {
                MarketServiceVO marketServiceVO = marketServiceClientOperator.queryMarketService(projectId, instanceDTO.getAppServiceId());
                detailVO.setAppServiceCode(marketServiceVO.getMarketServiceCode());
                detailVO.setAppServiceName(marketServiceVO.getMarketServiceName());
            }
        }
        DevopsEnvironmentDTO environmentDTO = environmentService.baseQueryById(centerEnvDTO.getEnvId());
        detailVO.setEnvCode(environmentDTO.getCode());
        detailVO.setEnvName(environmentDTO.getName());
        IamUserDTO userDTO = baseServiceClientOperator.queryUserByUserId(centerEnvDTO.getCreatedBy());
        detailVO.setUserId(userDTO.getId());
        BeanUtils.copyProperties(userDTO, detailVO);
        detailVO.setChartSourceValue(AppCenterChartSourceEnum.valueOf(centerEnvDTO.getChartSource()).getValue());
        detailVO.setChartSource(centerEnvDTO.getChartSource());
        return detailVO;
    }

    @Override
    public List<InstanceEventVO> envChartAppEvent(Long projectId, Long appCenterId) {
        DevopsDeployAppCenterEnvDTO centerEnvDTO = appCenterEnvMapper.selectByPrimaryKey(appCenterId);
        if (centerEnvDTO.getRdupmType().equals(AppCenterRdupmTypeEnum.CHART.getType())) {
            return devopsEnvResourceService.listInstancePodEvent(centerEnvDTO.getObjectId());
        }
        return null;
    }

    @Override
    public Page<DevopsEnvPodVO> envChartAppPodsPage(Long projectId, Long appCenterId, PageRequest pageRequest, String searchParam) {
        DevopsDeployAppCenterEnvDTO centerEnvDTO = appCenterEnvMapper.selectByPrimaryKey(appCenterId);
        if (centerEnvDTO.getRdupmType().equals(AppCenterRdupmTypeEnum.CHART.getType())) {
            AppServiceInstanceDTO instanceDTO = instanceService.baseQuery(centerEnvDTO.getObjectId());
            return devopsEnvPodService.pageByOptions(
                    projectId, instanceDTO.getEnvId(), instanceDTO.getAppServiceId(), centerEnvDTO.getObjectId(), pageRequest, searchParam);
        }
        return null;
    }

    @Override
    public DevopsEnvResourceVO envChartAppRelease(Long projectId, Long appCenterId) {
        DevopsDeployAppCenterEnvDTO centerEnvDTO = appCenterEnvMapper.selectByPrimaryKey(appCenterId);
        if (centerEnvDTO.getRdupmType().equals(AppCenterRdupmTypeEnum.CHART.getType())) {
            return appServiceInstanceService.listResourcesInHelmRelease(centerEnvDTO.getObjectId());
        }
        return null;
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
}
