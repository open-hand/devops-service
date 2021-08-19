package io.choerodon.devops.app.service;

import java.util.List;

import io.choerodon.core.domain.Page;
import io.choerodon.devops.api.vo.*;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;

/**
 * @Author: shanyu
 * @DateTime: 2021-08-18 15:27
 **/
public interface DevopsDeployAppCenterService {

    /**
     * 根据环境id分页查询所有应用，不传环境id表示查出所有有权限环境下的应用
     *
     * @param envId 环境id
     * @return 应用服务列表
     */
    Page<DevopsDeployAppCenterVO> listApp(Long projectId, Long envId, String name, String rdupmType, String operationType, String params, PageRequest pageable);

    AppCenterEnvDetailVO envAppDetail(Long projectId, Long appCenterId);

    List<InstanceEventVO> envChartAppEvent(Long projectId, Long appCenterId);

    Page<DevopsEnvPodVO> envChartAppPodsPage(Long projectId, Long appCenterId, PageRequest pageRequest, String searchParam);

    DevopsEnvResourceVO envChartAppRelease(Long projectId, Long appCenterId);

    Page<DevopsServiceVO> envChartService(Long projectId, Long appCenterId, PageRequest pageRequest, String searchParam);
}
