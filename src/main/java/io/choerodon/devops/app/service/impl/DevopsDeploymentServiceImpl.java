package io.choerodon.devops.app.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.choerodon.devops.app.service.DevopsDeploymentService;
import io.choerodon.devops.infra.mapper.DevopsDeploymentMapper;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2021/6/8 11:17
 */
@Service
public class DevopsDeploymentServiceImpl implements DevopsDeploymentService {
    @Autowired
    private DevopsDeploymentMapper devopsDeploymentMapper;
}
