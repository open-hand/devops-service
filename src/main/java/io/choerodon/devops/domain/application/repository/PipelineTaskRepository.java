package io.choerodon.devops.domain.application.repository;

import io.choerodon.devops.domain.application.entity.PipelineTaskE;

import java.util.List;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  10:12 2019/4/4
 * Description:
 */
public interface PipelineTaskRepository {
    PipelineTaskE create(PipelineTaskE pipelineTaskE);

    PipelineTaskE update(PipelineTaskE pipelineTaskE);

    void deleteById(Long pipelineTaskId);

    List<PipelineTaskE> queryByStageId(Long stageId);

    PipelineTaskE queryById(Long taskId);
}
