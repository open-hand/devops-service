package io.choerodon.devops.app.service;

import java.util.List;

import io.choerodon.devops.infra.dto.DevopsCiPipelineTriggerConfigVariableDTO;

public interface DevopsCiPipelineTriggerConfigVariableService {
    void baseCreate(DevopsCiPipelineTriggerConfigVariableDTO devopsCiPipelineVariableDTO);

    void deleteByPipelineTriggerConfigIds(List<Long> configIds);
}
