package io.choerodon.devops.app.service;

import io.choerodon.devops.infra.dto.DevopsCdJobValuesDTO;

public interface CiCdJobValuesServcie {
    void create(DevopsCdJobValuesDTO devopsCdJobValuesDTO);

    void deleteByPipelineId(Long ciCdPipelineId);
}
