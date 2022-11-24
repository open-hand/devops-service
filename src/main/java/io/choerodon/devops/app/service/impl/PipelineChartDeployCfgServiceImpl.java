package io.choerodon.devops.app.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.choerodon.devops.app.service.PipelineChartDeployCfgService;
import io.choerodon.devops.infra.mapper.PipelineChartDeployCfgMapper;

/**
 * chart部署任务配置表(PipelineChartDeployCfg)应用服务
 *
 * @author
 * @since 2022-11-24 15:57:06
 */
@Service
public class PipelineChartDeployCfgServiceImpl implements PipelineChartDeployCfgService {
    @Autowired
    private PipelineChartDeployCfgMapper pipelineChartDeployCfgMapper;

}

