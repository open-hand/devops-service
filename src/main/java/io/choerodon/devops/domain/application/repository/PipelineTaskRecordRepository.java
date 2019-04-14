package io.choerodon.devops.domain.application.repository;

import io.choerodon.devops.domain.application.entity.PipelineTaskRecordE;

import java.util.List;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  16:21 2019/4/9
 * Description:
 */
public interface PipelineTaskRecordRepository {
    PipelineTaskRecordE createOrUpdate(PipelineTaskRecordE taskRecordE);

    PipelineTaskRecordE queryById(Long taskRecordId);

    List<PipelineTaskRecordE> queryByStageRecordId(Long stageRecordId, Long taskId);
}
