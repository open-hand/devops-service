package io.choerodon.devops.app.service.impl;

import static io.choerodon.devops.infra.constant.ExceptionConstants.ClusterCode.DEVOPS_CLUSTER_NOT_EXIST;

import java.util.List;
import java.util.Objects;
import javax.annotation.Nullable;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.app.service.DevopsClusterProPermissionService;
import io.choerodon.devops.app.service.DevopsClusterService;
import io.choerodon.devops.infra.dto.DevopsClusterDTO;
import io.choerodon.devops.infra.dto.DevopsClusterProPermissionDTO;
import io.choerodon.devops.infra.mapper.DevopsClusterProPermissionMapper;

/**
 * @author zmf
 */
@Service
public class DevopsClusterProPermissionServiceImpl implements DevopsClusterProPermissionService {
    @Autowired
    private DevopsClusterProPermissionMapper devopsClusterProPermissionMapper;
    @Lazy
    @Autowired
    private DevopsClusterService devopsClusterService;

    @Override
    public void baseInsertPermission(DevopsClusterProPermissionDTO devopsClusterProPermissionDTO) {
        if (devopsClusterProPermissionMapper.insert(devopsClusterProPermissionDTO) != 1) {
            throw new CommonException("devops.devops.cluster.project.permission.add.error");
        }
    }

    @Nullable
    @Override
    public DevopsClusterProPermissionDTO queryPermission(Long projectId, Long clusterId) {
        DevopsClusterProPermissionDTO devopsClusterProPermissionDTO = new DevopsClusterProPermissionDTO();
        devopsClusterProPermissionDTO.setProjectId(Objects.requireNonNull(projectId));
        devopsClusterProPermissionDTO.setClusterId(Objects.requireNonNull(clusterId));
        return devopsClusterProPermissionMapper.selectOne(devopsClusterProPermissionDTO);
    }

    @Override
    public boolean projectHasClusterPermission(Long projectId, Long clusterId) {
        DevopsClusterDTO devopsClusterDTO = devopsClusterService.baseQuery(Objects.requireNonNull(clusterId));
        if (devopsClusterDTO == null) {
            throw new CommonException(DEVOPS_CLUSTER_NOT_EXIST, clusterId);
        }
        if (Boolean.TRUE.equals(devopsClusterDTO.getSkipCheckProjectPermission())) {
            return true;
        }
        return queryPermission(projectId, clusterId) != null;
    }

    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public void batchInsertIgnore(final Long clusterId, final List<Long> projectIds) {
        if (projectIds == null) {
            return;
        }

        DevopsClusterProPermissionDTO permission = new DevopsClusterProPermissionDTO();
        permission.setClusterId(clusterId);
        projectIds.forEach(p -> {
            permission.setId(null);
            permission.setProjectId(p);
            if (devopsClusterProPermissionMapper.selectOne(permission) == null) {
                devopsClusterProPermissionMapper.insert(permission);
            }
        });
    }

    @Override
    public List<DevopsClusterProPermissionDTO> baseListByClusterId(Long clusterId) {
        DevopsClusterProPermissionDTO devopsClusterProPermissionDTO = new DevopsClusterProPermissionDTO();
        devopsClusterProPermissionDTO.setClusterId(clusterId);
        return devopsClusterProPermissionMapper.select(devopsClusterProPermissionDTO);
    }

    @Override
    public void baseDeletePermissionByClusterIdAndProjectId(Long clusterId, Long projectId) {
        DevopsClusterProPermissionDTO devopsClusterProPermissionDTO = new DevopsClusterProPermissionDTO();
        devopsClusterProPermissionDTO.setProjectId(Objects.requireNonNull(projectId));
        devopsClusterProPermissionDTO.setClusterId(Objects.requireNonNull(clusterId));
        devopsClusterProPermissionMapper.delete(devopsClusterProPermissionDTO);
    }

    @Override
    public void baseDeleteByClusterId(Long clusterId) {
        DevopsClusterProPermissionDTO devopsClusterProPermissionDTO = new DevopsClusterProPermissionDTO();
        devopsClusterProPermissionDTO.setClusterId(Objects.requireNonNull(clusterId));
        devopsClusterProPermissionMapper.delete(devopsClusterProPermissionDTO);
    }
}
