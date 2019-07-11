package io.choerodon.devops.domain.application.repository;

import java.util.List;

import io.choerodon.devops.api.vo.iam.entity.DevopsClusterProPermissionE;

public interface DevopsClusterProPermissionRepository {


    void insert(DevopsClusterProPermissionE devopsClusterProPermissionE);

    List<DevopsClusterProPermissionE> listByClusterId(Long clusterId);

    void delete(DevopsClusterProPermissionE devopsClusterProPermissionE);

    void deleteByClusterId(Long clusterId);
}
