package io.choerodon.devops.domain.application.repository;

import java.util.List;

public interface DevopsClusterProPermissionRepository {


    void baseInsertPermission(DevopsClusterProPermissionE devopsClusterProPermissionE);

    List<DevopsClusterProPermissionE> baseListByClusterId(Long clusterId);

    void baseDeletePermission(DevopsClusterProPermissionE devopsClusterProPermissionE);

    void baseDeleteByClusterId(Long clusterId);
}
