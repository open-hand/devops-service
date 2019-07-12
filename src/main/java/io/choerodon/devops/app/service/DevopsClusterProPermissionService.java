package io.choerodon.devops.app.service;

import java.util.List;

import io.choerodon.devops.api.vo.iam.entity.DevopsClusterProPermissionE;
import io.choerodon.devops.infra.dto.DevopsClusterProPermissionDTO;

/**
 * @author zmf
 */
public interface DevopsClusterProPermissionService {
    void baseInsertPermission(DevopsClusterProPermissionDTO devopsClusterProPermissionDTO);

    List<DevopsClusterProPermissionE> baseListByClusterId(Long clusterId);

    void baseDeletePermission(DevopsClusterProPermissionDTO devopsClusterProPermissionDTO);

    void baseDeleteByClusterId(Long clusterId);
}
