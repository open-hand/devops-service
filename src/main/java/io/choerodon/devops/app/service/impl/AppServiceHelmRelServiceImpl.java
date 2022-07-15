package io.choerodon.devops.app.service.impl;

import org.springframework.beans.factory.annotation.Autowired;

import io.choerodon.devops.app.service.AppServiceHelmRelService;

import org.springframework.stereotype.Service;

import io.choerodon.devops.infra.mapper.AppServiceHelmRelMapper;

/**
 * 应用服务和helm配置的关联关系表(AppServiceHelmRel)应用服务
 *
 * @author hao.wang@zknow.com
 * @since 2022-07-15 10:55:52
 */
@Service
public class AppServiceHelmRelServiceImpl implements AppServiceHelmRelService {
    @Autowired
    private AppServiceHelmRelMapper appServiceHelmRelMapper;
}

