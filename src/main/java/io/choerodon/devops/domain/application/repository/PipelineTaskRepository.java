package io.choerodon.devops.domain.application.repository;

import io.choerodon.devops.domain.application.entity.PipelineTaskE;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  10:12 2019/4/4
 * Description:
 */
public interface PipelineTaskRepository {
    PipelineTaskE create(PipelineTaskE pipelineTaskE);

    PipelineTaskE update(PipelineTaskE pipelineTaskE);

    void deleteById(Long pipelineTaskId);
}
