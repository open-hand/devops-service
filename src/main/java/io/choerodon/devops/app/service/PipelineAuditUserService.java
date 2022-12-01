package io.choerodon.devops.app.service;

import java.util.List;

import io.choerodon.devops.infra.dto.PipelineAuditUserDTO;

/**
 * 人工卡点审核人员表(PipelineAuditUser)应用服务
 *
 * @author
 * @since 2022-11-24 15:56:49
 */
public interface PipelineAuditUserService {


    void batchCreateByConfigIdAndUserIds(Long id, List<Long> auditUserIds);

    List<PipelineAuditUserDTO> listByAuditConfigId(Long auditConfigId);

    void batchDeleteByConfigIds(List<Long> configIds);
}

