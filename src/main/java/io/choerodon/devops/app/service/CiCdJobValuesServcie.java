package io.choerodon.devops.app.service;

import io.choerodon.devops.infra.dto.CiCdJobValuesDTO;

public interface CiCdJobValuesServcie {
    void create(CiCdJobValuesDTO ciCdJobValuesDTO);

    void deleteByPipelineId(Long ciCdPipelineId);
}
