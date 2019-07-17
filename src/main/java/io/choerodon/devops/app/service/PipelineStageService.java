package io.choerodon.devops.app.service;

import io.choerodon.devops.infra.dto.PipelineStageDTO;

/**
 * Created by Sheep on 2019/7/15.
 */
public interface PipelineStageService {
    PipelineStageDTO baseQueryById(Long stageId);
}
