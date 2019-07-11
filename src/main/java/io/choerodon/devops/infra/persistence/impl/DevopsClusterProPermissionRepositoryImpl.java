package io.choerodon.devops.infra.persistence.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.choerodon.core.convertor.ConvertHelper;
import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.vo.iam.entity.DevopsClusterProPermissionE;
import io.choerodon.devops.domain.application.repository.DevopsClusterProPermissionRepository;
import io.choerodon.devops.infra.dto.DevopsClusterProPermissionDO;
import io.choerodon.devops.infra.mapper.DevopsClusterProPermissionMapper;

@Service
public class DevopsClusterProPermissionRepositoryImpl implements DevopsClusterProPermissionRepository {

    @Autowired
    DevopsClusterProPermissionMapper devopsClusterProPermissionMapper;

    @Override
    public void insert(DevopsClusterProPermissionE devopsClusterProPermissionE) {
        DevopsClusterProPermissionDO devopsClusterProPermissionDO = ConvertHelper.convert(devopsClusterProPermissionE, DevopsClusterProPermissionDO.class);
        if (devopsClusterProPermissionMapper.insert(devopsClusterProPermissionDO) != 1) {
            throw new CommonException("error.devops.cluster.project.permission.add.error");
        }
    }

    @Override
    public List<DevopsClusterProPermissionE> listByClusterId(Long clusterId) {
        DevopsClusterProPermissionDO devopsClusterProPermissionDO = new DevopsClusterProPermissionDO();
        devopsClusterProPermissionDO.setClusterId(clusterId);
        return ConvertHelper.convertList(devopsClusterProPermissionMapper.select(devopsClusterProPermissionDO), DevopsClusterProPermissionE.class);
    }

    @Override
    public void delete(DevopsClusterProPermissionE devopsClusterProPermissionE) {
        DevopsClusterProPermissionDO devopsClusterProPermissionDO = ConvertHelper.convert(devopsClusterProPermissionE, DevopsClusterProPermissionDO.class);
        devopsClusterProPermissionMapper.delete(devopsClusterProPermissionDO);
    }

    @Override
    public void deleteByClusterId(Long clusterId) {
        DevopsClusterProPermissionDO devopsClusterProPermissionDO = new DevopsClusterProPermissionDO();
        devopsClusterProPermissionDO.setClusterId(clusterId);
        devopsClusterProPermissionMapper.delete(devopsClusterProPermissionDO);
    }
}
