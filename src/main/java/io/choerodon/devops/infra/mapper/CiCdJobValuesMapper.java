package io.choerodon.devops.infra.mapper;

import org.apache.ibatis.annotations.Param;

import io.choerodon.devops.infra.dto.DevopsCdJobValuesDTO;
import io.choerodon.mybatis.common.BaseMapper;

public interface CiCdJobValuesMapper extends BaseMapper<DevopsCdJobValuesDTO> {


    void deleteByPipelineId(@Param("pipelineId") Long pipelineId);
}
