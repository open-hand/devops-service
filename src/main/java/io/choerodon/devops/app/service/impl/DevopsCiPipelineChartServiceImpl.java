package io.choerodon.devops.app.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.choerodon.devops.app.service.DevopsCiPipelineChartService;
import io.choerodon.devops.infra.mapper.DevopsCiPipelineChartMapper;

/**
 * ci任务生成chart记录(DevopsCiPipelineChart)应用服务
 *
 * @author hao.wang08@hand-china.com
 * @since 2021-12-15 17:35:12
 */
@Service
public class DevopsCiPipelineChartServiceImpl implements DevopsCiPipelineChartService {
    @Autowired
    private DevopsCiPipelineChartMapper devopsCiPipelineChartMapper;


}

