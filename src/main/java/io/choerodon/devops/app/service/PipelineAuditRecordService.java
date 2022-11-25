package io.choerodon.devops.app.service;

/**
 * 人工卡点审核记录表(PipelineAuditRecord)应用服务
 *
 * @author
 * @since 2022-11-23 16:42:03
 */
public interface PipelineAuditRecordService {

    void deleteByPipelineId(Long pipelineId);
}

