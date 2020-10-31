package io.choerodon.devops.app.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.choerodon.devops.infra.mapper.DevopsClusterOperationRecordMapper;

@Service
public class DevopsClusterOperationRecordServiceImpl implements DevopsClusterOperationRecordService {
    @Autowired
    private DevopsClusterOperationRecordMapper devopsClusterOperationRecordMapper;

    @Override
    public void deleteByClusterId(Long clusterId) {
        if (clusterId == null) {
            return;
        }
        devopsClusterOperationRecordMapper.deleteByClusterId(clusterId);
    }
}
