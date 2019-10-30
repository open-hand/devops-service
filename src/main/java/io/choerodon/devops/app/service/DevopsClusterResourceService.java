package io.choerodon.devops.app.service;

import io.choerodon.devops.infra.dto.DevopsClusterResourceDTO;

/**
 * @author zhaotianxin
 * @since 2019/10/29
 */
public interface DevopsClusterResourceService {
    void baseCreate(DevopsClusterResourceDTO devopsClusterResourceDTO);

    void baseUpdate(DevopsClusterResourceDTO devopsClusterResourceDTO);

    void operateCertManager(DevopsClusterResourceDTO devopsClusterResourceDTO, Long clusterId);

    DevopsClusterResourceDTO queryCertManager(Long clusterId);

    Boolean deleteCertManager(Long clusterId);
    
    DevopsClusterResourceDTO queryByClusterIdAndConfigId(Long clusterId, Long configId);

    DevopsClusterResourceDTO queryByClusterIdAndType(Long clusterId, String type);

    void delete(Long clusterId, Long configId);
}
