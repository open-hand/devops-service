package io.choerodon.devops.app.service.impl;

import java.util.List;

import io.choerodon.core.convertor.ConvertHelper;
import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.vo.iam.entity.DevopsClusterProPermissionE;
import io.choerodon.devops.app.service.DevopsClusterProPermissionService;
import io.choerodon.devops.infra.dto.DevopsClusterProPermissionDTO;
import io.choerodon.devops.infra.mapper.DevopsClusterProPermissionMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author zmf
 */
@Service
public class DevopsClusterProPermissionServiceImpl implements DevopsClusterProPermissionService {
    @Autowired
    DevopsClusterProPermissionMapper devopsClusterProPermissionMapper;

    @Override
    public void baseInsertPermission(DevopsClusterProPermissionDTO devopsClusterProPermissionDTO) {
        if (devopsClusterProPermissionMapper.insert(devopsClusterProPermissionDTO) != 1) {
            throw new CommonException("error.devops.cluster.project.permission.add.error");
        }
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
