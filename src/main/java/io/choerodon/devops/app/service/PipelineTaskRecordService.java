package io.choerodon.devops.app.service;

import java.util.List;

import io.choerodon.devops.infra.dto.PipelineTaskRecordDTO;

/**
 * @author zmf
 */
public interface PipelineTaskRecordService {
    PipelineTaskRecordDTO baseCreateOrUpdateRecord(PipelineTaskRecordDTO taskRecordDTO);

    PipelineTaskRecordDTO baseQueryRecordById(Long taskRecordId);

    List<PipelineTaskRecordDTO> baseQueryByStageRecordId(Long stageRecordId, Long taskId);

    void baseDeleteRecordById(Long recordId);

    List<PipelineTaskRecordDTO> baseQueryAllAutoTaskRecord(Long pipelineRecordId);
}
