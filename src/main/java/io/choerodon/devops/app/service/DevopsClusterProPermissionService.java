package io.choerodon.devops.app.service;

import java.util.List;

import io.choerodon.devops.infra.dto.DevopsClusterProPermissionDTO;

/**
 * @author zmf
 */
public interface DevopsClusterProPermissionService {
    void baseInsertPermission(DevopsClusterProPermissionDTO devopsClusterProPermissionDTO);

    /**
     * 批量插入，忽视已经存在的关联关系
     *
     * @param clusterId  集群id
     * @param projectIds 项目id
     */
    void batchInsertIgnore(Long clusterId, List<Long> projectIds);

    List<DevopsClusterProPermissionDTO> baseListByClusterId(Long clusterId);

    void baseDeletePermission(DevopsClusterProPermissionDTO devopsClusterProPermissionDTO);

    void baseDeleteByClusterId(Long clusterId);
}
