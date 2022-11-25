package io.choerodon.devops.app.service;

import io.choerodon.devops.infra.dto.PipelineAuditRecordDTO;

/**
 * 人工卡点审核记录表(PipelineAuditRecord)应用服务
 *
 * @author
 * @since 2022-11-23 16:42:03
 */
public interface PipelineAuditRecordService {

    void deleteByPipelineId(Long pipelineId);

    void baseCreate(PipelineAuditRecordDTO pipelineAuditRecordDTO);

    void initAuditRecord(Long pipelineId, Long jobRecordId, Long configId);
}

