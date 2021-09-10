package io.choerodon.devops.app.service;

import io.choerodon.devops.api.vo.DevopsDeployAppCenterEnvVO;
import io.choerodon.devops.api.vo.DevopsDeployGroupVO;

/**
 * @Author: shanyu
 * @DateTime: 2021-08-19 18:45
 **/
public interface DevopsDeployGroupService {
    /**
     * 创建或更新部署组应用
     *
     * @param projectId           项目id
     * @param devopsDeployGroupVO 部署组信息
     * @param operateType         操作类型
     * @return
     */
    DevopsDeployAppCenterEnvVO createOrUpdate(Long projectId, DevopsDeployGroupVO devopsDeployGroupVO, String operateType, boolean onlyForContainer);

    /**
     * 更新部署组容器配置
     * @param projectId
     * @param devopsDeployGroupVO
     */
    void updateContainer(Long projectId, DevopsDeployGroupVO devopsDeployGroupVO);
}
