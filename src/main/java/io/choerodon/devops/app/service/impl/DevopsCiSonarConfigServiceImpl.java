package io.choerodon.devops.app.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.choerodon.devops.app.service.DevopsCiSonarConfigService;
import io.choerodon.devops.infra.mapper.DevopsCiSonarConfigMapper;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2021/11/29 14:26
 */
@Service
public class DevopsCiSonarConfigServiceImpl implements DevopsCiSonarConfigService {
    @Autowired
    private DevopsCiSonarConfigMapper devopsCiSonarConfigMapper;
}
