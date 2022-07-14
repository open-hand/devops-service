package io.choerodon.devops.app.service.impl;

import org.springframework.beans.factory.annotation.Autowired;

import io.choerodon.devops.app.service.AppServiceHelmVersionService;

import org.springframework.stereotype.Service;

import io.choerodon.devops.infra.mapper.AppServiceHelmVersionMapper;

/**
 * 应用版本表(AppServiceHelmVersion)应用服务
 *
 * @author hao.wang@zknow.com
 * @since 2022-07-13 16:47:41
 */
@Service
public class AppServiceHelmVersionServiceImpl implements AppServiceHelmVersionService {
    @Autowired
    private AppServiceHelmVersionMapper appServiceHelmVersionMapper;
}

