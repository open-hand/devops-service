package io.choerodon.devops.app.service.impl;

import java.util.List;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.app.service.DevopsClusterProPermissionService;
import io.choerodon.devops.infra.dto.DevopsClusterProPermissionDTO;
import io.choerodon.devops.infra.mapper.DevopsClusterProPermissionMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author zmf
 */
@Service
public class DevopsClusterProPermissionServiceImpl implements DevopsClusterProPermissionService {
    @Autowired
    private DevopsClusterProPermissionMapper devopsClusterProPermissionMapper;

    @Override
    public void baseInsertPermission(DevopsClusterProPermissionDTO devopsClusterProPermissionDTO) {
        if (devopsClusterProPermissionMapper.insert(devopsClusterProPermissionDTO) != 1) {
            throw new CommonException("error.devops.cluster.project.permission.add.error");
        }
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
            permission.setProjectId(p);
            if (devopsClusterProPermissionMapper.selectByPrimaryKey(permission) != null) {
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
    public void baseDeletePermission(DevopsClusterProPermissionDTO devopsClusterProPermissionDTO) {
        devopsClusterProPermissionMapper.delete(devopsClusterProPermissionDTO);
    }

    @Override
    public void baseDeleteByClusterId(Long clusterId) {
        DevopsClusterProPermissionDTO devopsClusterProPermissionDTO = new DevopsClusterProPermissionDTO();
        devopsClusterProPermissionDTO.setClusterId(clusterId);
        devopsClusterProPermissionMapper.delete(devopsClusterProPermissionDTO);
    }
}
