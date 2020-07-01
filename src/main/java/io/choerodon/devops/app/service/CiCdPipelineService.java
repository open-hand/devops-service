package io.choerodon.devops.app.service;

import io.choerodon.devops.api.vo.CiCdPipelineVO;
import io.choerodon.devops.infra.dto.CiCdPipelineDTO;
import io.choerodon.devops.infra.dto.DevopsCiPipelineDTO;

public interface CiCdPipelineService {
    CiCdPipelineDTO create(Long projectId, CiCdPipelineVO ciCdPipelineVO);

    CiCdPipelineVO query(Long projectId, Long ciCdPipelineId);

    CiCdPipelineDTO update(Long projectId, Long ciCdPipelineId, CiCdPipelineVO ciCdPipelineVO);
}
