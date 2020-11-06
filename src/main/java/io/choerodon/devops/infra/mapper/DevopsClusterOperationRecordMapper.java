package io.choerodon.devops.infra.mapper;

import org.apache.ibatis.annotations.Param;

import io.choerodon.devops.infra.dto.DevopsClusterOperationRecordDTO;
import io.choerodon.mybatis.common.BaseMapper;

public interface DevopsClusterOperationRecordMapper extends BaseMapper<DevopsClusterOperationRecordDTO> {

    DevopsClusterOperationRecordDTO queryLatestRecordByNodeId(@Param("nodeId") Long nodeId);

    void deleteByClusterId(@Param("clusterId") Long clusterId);

    Long updateStatusByClusterId(@Param("clusterId") Long clusterId, @Param("oldStatus") String oldStatus, @Param("newStatus") String newStatus);

    DevopsClusterOperationRecordDTO selectByClusterIdAndType(@Param("clusterId") Long clusterId, @Param("type") String type);
}
