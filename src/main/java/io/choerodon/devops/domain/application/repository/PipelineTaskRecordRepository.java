package io.choerodon.devops.domain.application.repository;

import java.util.List;

import io.choerodon.devops.api.vo.iam.entity.PipelineTaskRecordE;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  16:21 2019/4/9
 * Description:
 */
public interface PipelineTaskRecordRepository {
    PipelineTaskRecordE createOrUpdate(PipelineTaskRecordE taskRecordE);

    PipelineTaskRecordE queryById(Long taskRecordId);

    List<PipelineTaskRecordE> queryByStageRecordId(Long stageRecordId, Long taskId);

    void delete(Long recordId);

    List<PipelineTaskRecordE> queryAllAutoTaskRecord(Long pipelineRecordId);

    PipelineTaskRecordE queryPendingCheckTask(Long stageRecordId);
}
