package io.choerodon.devops.app.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.choerodon.devops.app.service.PipelineJobService;
import io.choerodon.devops.infra.mapper.PipelineJobMapper;

/**
 * 流水线任务表(PipelineJob)应用服务
 *
 * @author
 * @since 2022-11-24 15:55:45
 */
@Service
public class PipelineJobServiceImpl implements PipelineJobService {
    @Autowired
    private PipelineJobMapper pipelineJobMapper;

}

