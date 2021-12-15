package io.choerodon.devops.app.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.choerodon.devops.app.service.DevopsCiPipelineSonarService;
import io.choerodon.devops.infra.mapper.DevopsCiPipelineSonarMapper;

/**
 * ci任务生成sonar记录(DevopsCiPipelineSonar)应用服务
 *
 * @author hao.wang08@hand-china.com
 * @since 2021-12-15 17:35:11
 */
@Service
public class DevopsCiPipelineSonarServiceImpl implements DevopsCiPipelineSonarService {
    @Autowired
    private DevopsCiPipelineSonarMapper devopsCiPipelineSonarMapper;


}

