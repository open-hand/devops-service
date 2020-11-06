package io.choerodon.devops.app.service;

import java.util.List;

import io.choerodon.devops.infra.dto.PipelineTaskDTO;

/**
 * @author zmf
 */
public interface PipelineTaskService {
    PipelineTaskDTO baseCreateTask(PipelineTaskDTO pipelineTaskDTO);

    PipelineTaskDTO baseUpdateTask(PipelineTaskDTO pipelineTaskDTO);

    void baseDeleteTaskById(Long pipelineTaskId);

    List<PipelineTaskDTO> baseQueryTaskByStageId(Long stageId);

    PipelineTaskDTO baseQueryTaskById(Long taskId);

    PipelineTaskDTO baseQueryTaskByAppDeployId(Long appDeployId);
}
