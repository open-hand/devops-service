package io.choerodon.devops.app.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.choerodon.devops.app.service.DevopsJobService;
import io.choerodon.devops.infra.mapper.DevopsJobMapper;


/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2021/6/8 11:21
 */
@Service
public class DevopsJobServiceImpl implements DevopsJobService {
    @Autowired
    private DevopsJobMapper devopsJobMapper;

}
