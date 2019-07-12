package io.choerodon.devops.domain.application.repository;

import java.util.List;

import io.choerodon.devops.api.vo.iam.entity.DevopsClusterProPermissionE;

public interface DevopsClusterProPermissionRepository {


    void baseInsertPermission(DevopsClusterProPermissionE devopsClusterProPermissionE);

    List<DevopsClusterProPermissionE> baseListByClusterId(Long clusterId);

    void baseDeletePermission(DevopsClusterProPermissionE devopsClusterProPermissionE);

    void baseDeleteByClusterId(Long clusterId);
}
