package io.choerodon.devops.app.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.choerodon.devops.app.service.PipelineAuditUserService;
import io.choerodon.devops.infra.mapper.PipelineAuditUserMapper;

/**
 * 人工卡点审核人员表(PipelineAuditUser)应用服务
 *
 * @author
 * @since 2022-11-24 15:56:49
 */
@Service
public class PipelineAuditUserServiceImpl implements PipelineAuditUserService {
    @Autowired
    private PipelineAuditUserMapper pipelineAuditUserMapper;

}

