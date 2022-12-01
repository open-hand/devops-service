package io.choerodon.devops.app.service;

import java.util.List;

import io.choerodon.devops.infra.dto.PipelineAuditUserRecordDTO;

/**
 * 人工卡点用户审核记录表(PipelineAuditUserRecord)应用服务
 *
 * @author
 * @since 2022-11-23 16:42:20
 */
public interface PipelineAuditUserRecordService {

    void deleteByPipelineId(Long pipelineId);

    void baseCreate(PipelineAuditUserRecordDTO pipelineAuditUserRecordDTO);

    List<PipelineAuditUserRecordDTO> listByAuditRecordId(Long auditRecordId);

    void baseUpdate(PipelineAuditUserRecordDTO pipelineAuditUserRecordDTO);
}

