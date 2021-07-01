package io.choerodon.devops.app.service.impl;

import io.choerodon.devops.app.service.DevopsJavaInstanceService;
import io.choerodon.devops.infra.mapper.DevopsJavaInstanceMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @Date 2021/7/1 9:25
 */
@Service
public class DevopsJavaInstanceServiceImpl implements DevopsJavaInstanceService {

    @Autowired
    private DevopsJavaInstanceMapper devopsJavaInstanceMapper;


}
