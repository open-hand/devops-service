package io.choerodon.devops.app.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.choerodon.devops.app.service.DevopsDaemonSetService;
import io.choerodon.devops.infra.mapper.DevopsDaemonSetMapper;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2021/6/8 11:19
 */
@Service
public class DevopsDaemonSetServiceImpl implements DevopsDaemonSetService {
    @Autowired
    private DevopsDaemonSetMapper devopsDaemonSetMapper;

}
