package io.choerodon.devops.app.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.choerodon.devops.app.service.PipelineStageRecordService;
import io.choerodon.devops.infra.mapper.PipelineStageRecordMapper;

/**
 * 流水线阶段记录(PipelineStageRecord)应用服务
 *
 * @author
 * @since 2022-11-23 16:43:13
 */
@Service
public class PipelineStageRecordServiceImpl implements PipelineStageRecordService {
    @Autowired
    private PipelineStageRecordMapper pipelineStageRecordMapper;

}

