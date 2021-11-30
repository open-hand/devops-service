package io.choerodon.devops.app.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.choerodon.devops.app.service.DevopsCiMavenPublishConfigService;
import io.choerodon.devops.infra.mapper.DevopsCiMavenPublishConfigMapper;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2021/11/30 18:05
 */
@Service
public class DevopsCiMavenPublishConfigServiceImpl implements DevopsCiMavenPublishConfigService {
    @Autowired
    private DevopsCiMavenPublishConfigMapper devopsCiMavenPublishConfigMapper;
}
