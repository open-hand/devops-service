package io.choerodon.devops.domain.application.repository;

import io.choerodon.devops.domain.application.entity.DevopsCheckLogE;

public interface DevopsCheckLogRepository {

    void create(DevopsCheckLogE devopsCheckLogE);
}
