package io.choerodon.devops.domain.application.repository;

import io.choerodon.devops.domain.application.entity.DevopsAppWebHookE;

public interface DevopsAppWebHookRepository {

    void createHook(DevopsAppWebHookE devopsAppWebHookE);

    DevopsAppWebHookE queryByAppId(Long appId);
}
