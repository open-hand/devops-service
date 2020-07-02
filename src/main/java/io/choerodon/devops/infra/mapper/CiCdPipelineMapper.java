package io.choerodon.devops.infra.mapper;

import org.apache.ibatis.annotations.Param;

import io.choerodon.devops.infra.dto.CiCdPipelineDTO;
import io.choerodon.mybatis.common.BaseMapper;

public interface CiCdPipelineMapper extends BaseMapper<CiCdPipelineDTO> {
    /**
     * 停用流水线
     */
    int disablePipeline(@Param("ciCdPipelineId") Long ciCdPipelineId);

    /**
     * 启用流水线
     */
    int enablePipeline(@Param("ciCdPipelineId") Long ciCdPipelineId);
}
