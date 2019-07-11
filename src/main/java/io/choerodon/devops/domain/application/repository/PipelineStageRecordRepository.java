package io.choerodon.devops.domain.application.repository;

import io.choerodon.devops.api.vo.iam.entity.PipelineStageRecordE;

import java.util.List;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  16:04 2019/4/4
 * Description:
 */
public interface PipelineStageRecordRepository {
    List<PipelineStageRecordE> list(Long projectId, Long pipelineRecordId);

    PipelineStageRecordE createOrUpdate(PipelineStageRecordE stageRecordE);

    List<PipelineStageRecordE> queryByPipeRecordId(Long pipelineRecordId, Long stageId);

    PipelineStageRecordE queryById(Long recordId);

    PipelineStageRecordE update(PipelineStageRecordE stageRecordE);

    PipelineStageRecordE queryPendingCheck(Long pipelineRecordId);
}
