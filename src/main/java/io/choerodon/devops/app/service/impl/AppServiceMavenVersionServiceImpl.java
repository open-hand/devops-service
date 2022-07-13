package io.choerodon.devops.app.service.impl;

import org.springframework.beans.factory.annotation.Autowired;

import io.choerodon.devops.app.service.AppServiceMavenVersionService;

import org.springframework.stereotype.Service;

import io.choerodon.devops.infra.mapper.AppServiceMavenVersionMapper;

/**
 * 应用版本表(AppServiceMavenVersion)应用服务
 *
 * @author hao.wang@zknow.com
 * @since 2022-07-13 16:47:43
 */
@Service
public class AppServiceMavenVersionServiceImpl implements AppServiceMavenVersionService {
    @Autowired
    private AppServiceMavenVersionMapper appServiceMavenVersionMapper;
}

