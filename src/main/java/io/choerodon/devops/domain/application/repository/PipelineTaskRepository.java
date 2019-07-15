package io.choerodon.devops.domain.application.repository;

import io.choerodon.devops.api.vo.iam.entity.PipelineTaskE;

import java.util.List;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  10:12 2019/4/4
 * Description:
 */
public interface PipelineTaskRepository {
    PipelineTaskE baseCreateTask(PipelineTaskE pipelineTaskE);

    PipelineTaskE baseUpdateTask(PipelineTaskE pipelineTaskE);

    void baseDeleteTaskById(Long pipelineTaskId);

    List<PipelineTaskE> baseQueryTaskByStageId(Long stageId);

    PipelineTaskE baseQueryTaskById(Long taskId);

    PipelineTaskE baseQueryTaskByAppDeployId(Long appDeployId);
}
