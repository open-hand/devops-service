package io.choerodon.devops.app.service;

import io.choerodon.devops.infra.dto.DevopsClusterResourceDTO;

/**
 * @author zhaotianxin
 * @since 2019/10/29
 */
public interface DevopsClusterResourceService {
    void baseCreateOrUpdate(DevopsClusterResourceDTO devopsClusterResourceDTO);

    void create(DevopsClusterResourceDTO devopsClusterResourceDTO, Long clusterId);

    DevopsClusterResourceDTO queryByClusterIdAndConfigId(Long clusterId, Long configId);

    DevopsClusterResourceDTO queryByClusterIdAndType(Long clusterId,String type);
}
