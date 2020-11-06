package io.choerodon.devops.app.service;

import io.choerodon.devops.infra.dto.DevopsClusterOperationRecordDTO;

public interface DevopsClusterOperationRecordService {
    void deleteByClusterId(Long clusterId);

    DevopsClusterOperationRecordDTO selectByClusterIdAndType(Long clusterId, String type);

    void updateByPrimaryKeySelective(DevopsClusterOperationRecordDTO devopsClusterOperationRecordDTO);

}
