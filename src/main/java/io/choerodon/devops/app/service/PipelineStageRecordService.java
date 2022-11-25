package io.choerodon.devops.app.service;

import java.util.List;

import io.choerodon.devops.infra.dto.PipelineStageRecordDTO;

/**
 * 流水线阶段记录(PipelineStageRecord)应用服务
 *
 * @author
 * @since 2022-11-23 16:43:13
 */
public interface PipelineStageRecordService {

    void deleteByPipelineId(Long pipelineId);

    void baseCreate(PipelineStageRecordDTO pipelineStageRecordDTO);

    List<PipelineStageRecordDTO> listByPipelineRecordId(Long pipelineRecordId);

    void baseUpdate(PipelineStageRecordDTO firstStageRecordDTO);
}

