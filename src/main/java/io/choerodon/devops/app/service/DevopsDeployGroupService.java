package io.choerodon.devops.app.service;

import io.choerodon.devops.api.vo.DevopsDeployGroupVO;
import io.choerodon.devops.infra.dto.DevopsDeployGroupDTO;
import io.choerodon.devops.infra.dto.DevopsEnvironmentDTO;
import io.choerodon.devops.infra.dto.iam.ProjectDTO;

/**
 * @Author: shanyu
 * @DateTime: 2021-08-19 18:45
 **/
public interface DevopsDeployGroupService {

    /**
     * 查询应用和容器配置信息
     *
     * @param projectId           项目id
     * @param devopsConfigGroupId devops配置组id
     * @return
     */
    DevopsDeployGroupVO appConfigDetail(Long projectId, Long devopsConfigGroupId);

    /**
     * 创建或更新部署组应用
     *
     * @param projectId           项目id
     * @param devopsDeployGroupVO 部署组信息
     * @param operateType         操作类型
     * @return
     */
    DevopsDeployGroupDTO createOrUpdate(Long projectId, DevopsDeployGroupVO devopsDeployGroupVO, String operateType);
}
