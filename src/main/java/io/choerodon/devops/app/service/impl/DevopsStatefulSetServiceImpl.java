package io.choerodon.devops.app.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.choerodon.devops.app.service.DevopsStatefulSetService;
import io.choerodon.devops.infra.mapper.DevopsStatefulSetMapper;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2021/6/8 11:20
 */
@Service
public class DevopsStatefulSetServiceImpl implements DevopsStatefulSetService {
    @Autowired
    private DevopsStatefulSetMapper devopsStatefulSetMapper;
}
