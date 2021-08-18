package io.choerodon.devops.app.service.impl;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.choerodon.devops.api.vo.AppCenterEnvDetailVO;
import io.choerodon.devops.api.vo.market.MarketServiceVO;
import io.choerodon.devops.app.service.AppCenterService;
import io.choerodon.devops.app.service.AppServiceInstanceService;
import io.choerodon.devops.app.service.AppServiceService;
import io.choerodon.devops.app.service.DevopsEnvironmentService;
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

/**
 * @Author: scp
 * @Description:
 * @Date: Created in 2021/8/18
 * @Modified By:
 */
@Service
public class AppCenterServiceImpl implements AppCenterService {
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

    @Override
    public AppCenterEnvDetailVO appCenterDetail(Long projectId, Long appCenterId) {
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
}
