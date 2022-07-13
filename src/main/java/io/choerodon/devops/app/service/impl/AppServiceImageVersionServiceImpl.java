package io.choerodon.devops.app.service.impl;

import org.springframework.beans.factory.annotation.Autowired;

import io.choerodon.devops.app.service.AppServiceImageVersionService;

import org.springframework.stereotype.Service;

import io.choerodon.devops.infra.mapper.AppServiceImageVersionMapper;

/**
 * 应用版本表(AppServiceImageVersion)应用服务
 *
 * @author hao.wang@zknow.com
 * @since 2022-07-13 16:47:42
 */
@Service
public class AppServiceImageVersionServiceImpl implements AppServiceImageVersionService {
    @Autowired
    private AppServiceImageVersionMapper appServiceImageVersionMapper;
}

