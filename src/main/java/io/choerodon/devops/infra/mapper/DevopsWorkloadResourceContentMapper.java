package io.choerodon.devops.infra.mapper;

import org.apache.ibatis.annotations.Param;

import io.choerodon.devops.infra.dto.DevopsWorkloadResourceContentDTO;
import io.choerodon.mybatis.common.BaseMapper;

public interface DevopsWorkloadResourceContentMapper extends BaseMapper<DevopsWorkloadResourceContentDTO> {
    void deleteByResourceId(@Param("type") String type, @Param("workloadId") Long workloadId);
}
