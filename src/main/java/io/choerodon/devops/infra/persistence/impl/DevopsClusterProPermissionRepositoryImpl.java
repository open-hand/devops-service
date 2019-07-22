package io.choerodon.devops.infra.persistence.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.choerodon.core.convertor.ConvertHelper;
import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.domain.application.repository.DevopsClusterProPermissionRepository;
import io.choerodon.devops.infra.dto.DevopsClusterProPermissionDTO;
import io.choerodon.devops.infra.mapper.DevopsClusterProPermissionMapper;

@Service
public class DevopsClusterProPermissionRepositoryImpl implements DevopsClusterProPermissionRepository {

    @Autowired
    DevopsClusterProPermissionMapper devopsClusterProPermissionMapper;

    @Override
    public void baseInsertPermission(DevopsClusterProPermissionE devopsClusterProPermissionE) {
        DevopsClusterProPermissionDTO devopsClusterProPermissionDTO = ConvertHelper.convert(devopsClusterProPermissionE, DevopsClusterProPermissionDTO.class);
        if (devopsClusterProPermissionMapper.insert(devopsClusterProPermissionDTO) != 1) {
            throw new CommonException("error.devops.cluster.project.permission.add.error");
        }
    }

    @Override
    public List<DevopsClusterProPermissionE> baseListByClusterId(Long clusterId) {
        DevopsClusterProPermissionDTO devopsClusterProPermissionDTO = new DevopsClusterProPermissionDTO();
        devopsClusterProPermissionDTO.setClusterId(clusterId);
        return ConvertHelper.convertList(devopsClusterProPermissionMapper.select(devopsClusterProPermissionDTO), DevopsClusterProPermissionE.class);
    }

    @Override
    public void baseDeletePermission(DevopsClusterProPermissionE devopsClusterProPermissionE) {
        DevopsClusterProPermissionDTO devopsClusterProPermissionDTO = ConvertHelper.convert(devopsClusterProPermissionE, DevopsClusterProPermissionDTO.class);
        devopsClusterProPermissionMapper.delete(devopsClusterProPermissionDTO);
    }

    @Override
    public void baseDeleteByClusterId(Long clusterId) {
        DevopsClusterProPermissionDTO devopsClusterProPermissionDTO = new DevopsClusterProPermissionDTO();
        devopsClusterProPermissionDTO.setClusterId(clusterId);
        devopsClusterProPermissionMapper.delete(devopsClusterProPermissionDTO);
    }
}
