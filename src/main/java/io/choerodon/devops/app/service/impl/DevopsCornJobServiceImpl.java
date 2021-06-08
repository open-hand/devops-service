package io.choerodon.devops.app.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.choerodon.devops.app.service.DevopsCornJobService;
import io.choerodon.devops.infra.mapper.DevopsCornJobMapper;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2021/6/8 11:22
 */
@Service
public class DevopsCornJobServiceImpl implements DevopsCornJobService {
    @Autowired
    private DevopsCornJobMapper devopsCornJobMapper;
}
