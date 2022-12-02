package io.choerodon.devops.infra.mapper;

import java.util.Set;

import org.apache.ibatis.annotations.Param;

import io.choerodon.devops.infra.dto.PipelineStageRecordDTO;
import io.choerodon.mybatis.common.BaseMapper;


/**
 * 流水线阶段记录(PipelineStageRecord)应用服务
 *
 * @author
 * @since 2022-11-23 16:43:12
 */
public interface PipelineStageRecordMapper extends BaseMapper<PipelineStageRecordDTO> {

    void cancelPipelineStages(@Param("pipelineRecordId") Long pipelineRecordId);

    PipelineStageRecordDTO queryByIdForUpdate(@Param("id") Long id);

    void updateCanceledAndFailedStatusToCreated(@Param("pipelineRecordId") Long pipelineRecordId);

    void cancelPipelineStagesByIds(@Param("stageRecordIds") Set<Long> stageRecordIds);
}

