package io.choerodon.devops.app.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.choerodon.devops.app.service.PipelineAuditUserRecordService;
import io.choerodon.devops.infra.mapper.PipelineAuditUserRecordMapper;

/**
 * 人工卡点用户审核记录表(PipelineAuditUserRecord)应用服务
 *
 * @author
 * @since 2022-11-23 16:42:20
 */
@Service
public class PipelineAuditUserRecordServiceImpl implements PipelineAuditUserRecordService {
    @Autowired
    private PipelineAuditUserRecordMapper pipelineAuditUserRecordMapper;

}

