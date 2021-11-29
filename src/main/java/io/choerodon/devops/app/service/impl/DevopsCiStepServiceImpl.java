package io.choerodon.devops.app.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.choerodon.devops.app.service.DevopsCiStepService;
import io.choerodon.devops.infra.mapper.DevopsCiStepMapper;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2021/11/29 14:22
 */
@Service
public class DevopsCiStepServiceImpl implements DevopsCiStepService {
    @Autowired
    private DevopsCiStepMapper devopsCiStepMapper;
}
