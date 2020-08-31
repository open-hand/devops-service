package io.choerodon.devops.app.service;


import java.util.List;

import io.choerodon.devops.infra.dto.DevopsCdAuditDTO;

public interface DevopsCdAuditService {
    void baseCreate(DevopsCdAuditDTO devopsCdAuditDTO);

    List<DevopsCdAuditDTO> baseListByOptions(Long pipelineId, Long stageId, Long jobId);

    void fixProjectId();
}
