package io.choerodon.devops.app.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.choerodon.devops.app.service.DevopsCiPipelineVariableService;
import io.choerodon.devops.infra.mapper.DevopsCiPipelineVariableMapper;

/**
 * 流水线配置的CI变量(DevopsCiPipelineVariable)应用服务
 *
 * @author hao.wang08@hand-china.com
 * @since 2021-12-03 16:18:15
 */
@Service
public class DevopsCiPipelineVariableServiceImpl implements DevopsCiPipelineVariableService {
    @Autowired
    private DevopsCiPipelineVariableMapper devopsCiPipelineVariablemapper;


}

