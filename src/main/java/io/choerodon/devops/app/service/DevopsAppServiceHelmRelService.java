package io.choerodon.devops.app.service;

import io.choerodon.devops.infra.dto.DevopsAppServiceHelmRelDTO;

public interface DevopsAppServiceHelmRelService {

    /**
     * 处理应用服务和helm仓库的关联关系
     */
    void handleRel(Long appServiceId, Long helmConfigId);

    /**
     * 删除应用服务和helm仓库的关联关系
     *
     * @param appServiceId
     */
    void deleteRelationByServiceId(Long appServiceId);

    /**
     * 创建应用符合和helm仓库的关联关系
     *
     * @param appServiceId
     * @param helmConfigId
     */
    void createRel(Long appServiceId, Long helmConfigId);

    DevopsAppServiceHelmRelDTO queryByAppServiceId(Long appServiceId);
}
