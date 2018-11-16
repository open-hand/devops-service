package io.choerodon.devops.domain.application.repository;

import java.util.List;

import io.choerodon.devops.domain.application.entity.DevopsCheckLogE;
import io.choerodon.devops.infra.dataobject.DevopsProjectDO;

public interface DevopsCheckLogRepository {

    void create(DevopsCheckLogE devopsCheckLogE);

    List<DevopsProjectDO> queryNonEnvGroupProject();
}
