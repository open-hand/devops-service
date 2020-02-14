package io.choerodon.devops.app.service;

import java.util.List;

import javax.annotation.Nullable;

import io.choerodon.devops.infra.dto.DevopsClusterProPermissionDTO;

/**
 * @author zmf
 */
public interface DevopsClusterProPermissionService {
    void baseInsertPermission(DevopsClusterProPermissionDTO devopsClusterProPermissionDTO);

    /**
     * 查询项目和集群的权限关系
     *
     * @param projectId 项目id
     * @param clusterId 集群id
     * @return 权限关系(可为空)
     */
    @Nullable
    DevopsClusterProPermissionDTO queryPermission(Long projectId, Long clusterId);

    /**
     * 查询此项目是否有集群的权限
     *
     * @param projectId 项目id
     * @param clusterId 集群id
     * @return true表示有权限, false没有
     */
    boolean projectHasClusterPermission(Long projectId, Long clusterId);

    /**
     * 批量插入，忽视已经存在的关联关系
     *
     * @param clusterId  集群id
     * @param projectIds 项目id
     */
    void batchInsertIgnore(Long clusterId, List<Long> projectIds);

    List<DevopsClusterProPermissionDTO> baseListByClusterId(Long clusterId);

    /**
     * 删除纪录
     *
     * @param clusterId  集群id
     * @param projectId 项目id
     */
    void baseDeletePermissionByClusterIdAndProjectId(Long clusterId, Long projectId);

    void baseDeleteByClusterId(Long clusterId);
}
