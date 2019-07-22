package io.choerodon.devops.app.service;

import java.util.List;

import io.choerodon.devops.infra.dto.PipelineStageRecordDTO;

/**
 * Created by Sheep on 2019/7/15.
 */
public interface PipelineStageRecordService {

    List<PipelineStageRecordDTO> baseListByRecordAndStageId(Long pipelineRecordId, Long stageId);

    PipelineStageRecordDTO baseQueryById(Long recordId);

    PipelineStageRecordDTO baseCreateOrUpdate(PipelineStageRecordDTO pipelineStageRecordDTO);

    PipelineStageRecordDTO baseUpdate(PipelineStageRecordDTO pipelineStageRecordDTO);

}
