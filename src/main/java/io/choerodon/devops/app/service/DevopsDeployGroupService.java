package io.choerodon.devops.app.service;

import io.choerodon.devops.api.vo.DevopsDeployGroupVO;

/**
 * @Author: shanyu
 * @DateTime: 2021-08-19 18:45
 **/
public interface DevopsDeployGroupService {

    /**
     * 查询应用和容器配置信息
     *
     * @param projectId 项目id
     * @param devopsConfigGroupId devops配置组id
     * @return
     */
    DevopsDeployGroupVO appConfigDetail(Long projectId, Long devopsConfigGroupId);
}
