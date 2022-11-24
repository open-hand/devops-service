package io.choerodon.devops.app.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.choerodon.devops.app.service.PipelineVersionService;
import io.choerodon.devops.infra.mapper.PipelineVersionMapper;

/**
 * 流水线版本表(PipelineVersion)应用服务
 *
 * @author
 * @since 2022-11-24 15:57:18
 */
@Service
public class PipelineVersionServiceImpl implements PipelineVersionService {
    @Autowired
    private PipelineVersionMapper pipelineVersionMapper;

}

