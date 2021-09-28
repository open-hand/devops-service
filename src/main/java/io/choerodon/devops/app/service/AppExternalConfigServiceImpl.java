package io.choerodon.devops.app.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.choerodon.devops.infra.mapper.AppExternalConfigMapper;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2021/9/28 10:57
 */
@Service
public class AppExternalConfigServiceImpl implements AppExternalConfigService {
    @Autowired
    private AppExternalConfigMapper appExternalConfigMapper;
}
