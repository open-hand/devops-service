package io.choerodon.devops.app.service;

import io.choerodon.devops.infra.dto.DevopsCiPipelineTriggerConfigVariableDTO;

public interface DevopsCiPipelineTriggerConfigVariableService {
    void baseCreate(DevopsCiPipelineTriggerConfigVariableDTO devopsCiPipelineVariableDTO);

    void deleteByPipelineId(Long pipelineId);
}
