package io.choerodon.devops.app.service;

import java.util.List;

import io.choerodon.devops.infra.dto.PipelineStageDTO;

/**
 * Created by Sheep on 2019/7/15.
 */
public interface PipelineStageService {
    PipelineStageDTO baseQueryById(Long stageId);

    PipelineStageDTO baseCreate(PipelineStageDTO pipelineStageDTO);

    List<PipelineStageDTO> baseListByPipelineId(Long pipelineId);

    void baseDelete(Long stageId);

    PipelineStageDTO baseUpdate(PipelineStageDTO pipelineStageDTO);
}
