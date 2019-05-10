package io.choerodon.devops.domain.application.repository;

import io.choerodon.devops.domain.application.entity.DevopsEnvCommandValueE;

public interface DevopsEnvCommandValueRepository {
    DevopsEnvCommandValueE create(DevopsEnvCommandValueE devopsEnvCommandValueE);

    void deleteById(Long commandId);
}
