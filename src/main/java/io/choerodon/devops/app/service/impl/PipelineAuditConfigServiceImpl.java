package io.choerodon.devops.app.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.choerodon.devops.app.service.PipelineAuditConfigService;
import io.choerodon.devops.infra.mapper.PipelineAuditConfigMapper;

/**
 * 人工卡点配置表(PipelineAuditConfig)应用服务
 *
 * @author
 * @since 2022-11-24 15:56:37
 */
@Service
public class PipelineAuditConfigServiceImpl implements PipelineAuditConfigService {
    @Autowired
    private PipelineAuditConfigMapper pipelineAuditConfigMapper;

}

