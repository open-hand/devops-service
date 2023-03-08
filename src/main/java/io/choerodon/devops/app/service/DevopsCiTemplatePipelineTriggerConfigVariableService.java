package io.choerodon.devops.app.service;

import java.util.List;

import io.choerodon.devops.infra.dto.DevopsCiTemplatePipelineTriggerConfigVariableDTO;

public interface DevopsCiTemplatePipelineTriggerConfigVariableService {
    void baseCreate(DevopsCiTemplatePipelineTriggerConfigVariableDTO devopsCiPipelineVariableDTO);

    void deleteByPipelineTriggerConfigIds(List<Long> configIds);
}
