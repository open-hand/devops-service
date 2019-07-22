package io.choerodon.devops.domain.application.repository;

import java.util.List;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  16:21 2019/4/9
 * Description:
 */
public interface PipelineTaskRecordRepository {
    PipelineTaskRecordE baseCreateOrUpdateRecord(PipelineTaskRecordE taskRecordE);

    PipelineTaskRecordE baseQueryRecordById(Long taskRecordId);

    List<PipelineTaskRecordE> baseQueryByStageRecordId(Long stageRecordId, Long taskId);

    void baseDeleteRecordById(Long recordId);

    List<PipelineTaskRecordE> baseQueryAllAutoTaskRecord(Long pipelineRecordId);

    PipelineTaskRecordE baseQueryPendingCheckTask(Long stageRecordId);
}
