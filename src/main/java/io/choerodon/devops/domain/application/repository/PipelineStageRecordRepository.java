package io.choerodon.devops.domain.application.repository;

import io.choerodon.devops.infra.dto.PipelineStageRecordDTO;

import java.util.List;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  16:04 2019/4/4
 * Description:
 */
public interface PipelineStageRecordRepository {
    List<PipelineStageRecordDTO> baseListByRecordId(Long projectId, Long pipelineRecordId);

    PipelineStageRecordDTO baseCreateOrUpdate(PipelineStageRecordDTO pipelineStageRecordDTO);

    List<PipelineStageRecordDTO> baseListByRecordAndStageId(Long pipelineRecordId, Long stageId);

    PipelineStageRecordDTO baseQueryByRecordId(Long recordId);

    PipelineStageRecordDTO baseUpdate(PipelineStageRecordE pipelineStageRecordE);

    PipelineStageRecordE baseQueryByPendingCheckStatus(Long pipelineRecordId);
}
