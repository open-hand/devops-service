package io.choerodon.devops.app.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.choerodon.devops.app.service.PipelineAuditRecordService;
import io.choerodon.devops.infra.mapper.PipelineAuditRecordMapper;

/**
 * 人工卡点审核记录表(PipelineAuditRecord)应用服务
 *
 * @author
 * @since 2022-11-23 16:42:03
 */
@Service
public class PipelineAuditRecordServiceImpl implements PipelineAuditRecordService {
    @Autowired
    private PipelineAuditRecordMapper pipelineAuditRecordMapper;

}

