package io.choerodon.devops.app.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.choerodon.devops.app.service.PipelineStageService;
import io.choerodon.devops.infra.mapper.PipelineStageMapper;

/**
 * 流水线阶段表(PipelineStage)应用服务
 *
 * @author
 * @since 2022-11-24 15:52:49
 */
@Service
public class PipelineStageServiceImpl implements PipelineStageService {
    @Autowired
    private PipelineStageMapper pipelineStageMapper;

}

