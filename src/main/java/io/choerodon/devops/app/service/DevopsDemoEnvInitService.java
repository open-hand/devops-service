package io.choerodon.devops.app.service;

import io.choerodon.devops.domain.application.event.OrganizationRegisterEventPayload;

/**
 * @author zmf
 */
public interface DevopsDemoEnvInitService {

    /**
     * 搭建Demo环境项目初始化数据
     *
     * @param organizationRegisterEventPayload 组织和项目的创建相关信息
     */
    void initialDemoEnv(OrganizationRegisterEventPayload organizationRegisterEventPayload);
}
