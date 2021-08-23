package io.choerodon.devops.app.service;

import java.util.List;

import io.choerodon.core.domain.Page;
import io.choerodon.devops.api.vo.*;
import io.choerodon.devops.infra.dto.DevopsDeployAppCenterEnvDTO;
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

    List<InstanceEventVO> envAppEvent(Long projectId, Long appCenterId);

    Page<DevopsEnvPodVO> envAppPodsPage(Long projectId, Long appCenterId, PageRequest pageRequest, String searchParam);

    DevopsEnvResourceVO envAppRelease(Long projectId, Long appCenterId);

    Page<DevopsServiceVO> envChartService(Long projectId, Long appCenterId, PageRequest pageRequest, String searchParam);

    /**
     * 创建应用
     *
     * @param devopsDeployAppCenterEnvDTO
     */
    void baseCreate(DevopsDeployAppCenterEnvDTO devopsDeployAppCenterEnvDTO);

    /**
     * 更新应用
     *
     * @param devopsDeployAppCenterEnvDTO
     */
    void baseUpdate(DevopsDeployAppCenterEnvDTO devopsDeployAppCenterEnvDTO);

    DevopsDeployAppCenterEnvDTO queryByEnvIdAndCode(Long environmentId, String appCode);
}
