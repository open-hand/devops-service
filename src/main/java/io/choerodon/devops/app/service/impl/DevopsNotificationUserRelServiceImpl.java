package io.choerodon.devops.app.service.impl;

import io.choerodon.devops.app.service.DevopsNotificationUserRelService;
import io.choerodon.devops.infra.mapper.DevopsNotificationUserRelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by Sheep on 2019/7/15.
 */

@Service
public class DevopsNotificationUserRelServiceImpl implements DevopsNotificationUserRelService {

    @Autowired
    private DevopsNotificationUserRelMapper devopsNotificationUserRelMapper;

}
