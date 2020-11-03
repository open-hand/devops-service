package io.choerodon.devops.app.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.choerodon.devops.infra.dto.DevopsClusterOperationRecordDTO;
import io.choerodon.devops.infra.mapper.DevopsClusterOperationRecordMapper;
import io.choerodon.devops.infra.util.MapperUtil;

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

    @Override
    public DevopsClusterOperationRecordDTO selectByClusterIdAndType(Long clusterId, String type) {
        return devopsClusterOperationRecordMapper.selectByClusterIdAndType(clusterId, type);
    }

    @Override
    public void updateByPrimaryKeySelective(DevopsClusterOperationRecordDTO devopsClusterOperationRecordDTO) {
        MapperUtil.resultJudgedUpdateByPrimaryKey(devopsClusterOperationRecordMapper, devopsClusterOperationRecordDTO, "error.update.clusterOperation");
    }
}
