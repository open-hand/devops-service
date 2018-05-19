package io.choerodon.devops.domain.application.repository;

import io.choerodon.devops.domain.application.entity.DevopsEnvCommandE;

public interface DevopsEnvCommandRepository {

    DevopsEnvCommandE create(DevopsEnvCommandE devopsEnvCommandE);

    DevopsEnvCommandE queryByObject(String objectType, Long objectId);

    DevopsEnvCommandE update(DevopsEnvCommandE devopsEnvCommandE);
}
