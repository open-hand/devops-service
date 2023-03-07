package io.choerodon.devops.infra.mapper;

import org.apache.ibatis.annotations.Param;

import io.choerodon.devops.infra.dto.PipelineLogDTO;
import io.choerodon.mybatis.common.BaseMapper;

/**
 * 流水线执行日志(PipelineLog)应用服务
 *
 * @author
 * @since 2022-11-23 16:42:45
 */
public interface PipelineLogMapper extends BaseMapper<PipelineLogDTO> {

    String queryLastedByJobRecordId(@Param("jobRecordId") Long jobRecordId);
}

