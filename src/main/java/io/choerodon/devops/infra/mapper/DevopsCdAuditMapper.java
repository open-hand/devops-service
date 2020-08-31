package io.choerodon.devops.infra.mapper;

import org.apache.ibatis.annotations.Param;

import io.choerodon.devops.infra.dto.DevopsCdAuditDTO;
import io.choerodon.mybatis.common.BaseMapper;

public interface DevopsCdAuditMapper extends BaseMapper<DevopsCdAuditDTO> {
    Integer updateProjectIdByJobId(@Param("projectId") Long projectId, @Param("jobId") Long jobId);
}
