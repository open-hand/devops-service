package io.choerodon.devops.app.service;

import java.util.List;

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

    void initAuditRecord(Long pipelineId, Long pipelineRecordId, Long jobRecordId, Long configId);

    PipelineAuditRecordDTO queryByJobRecordId(Long jobRecordId);

    List<PipelineAuditRecordDTO> listByPipelineRecordId(Long pipelineRecordId);

    PipelineAuditRecordDTO queryByJobRecordIdForUpdate(Long jobRecordId);

    void sendJobAuditMessage(Long pipelineId, Long pipelineRecordId, String stageName, Long jobRecordId);
}

