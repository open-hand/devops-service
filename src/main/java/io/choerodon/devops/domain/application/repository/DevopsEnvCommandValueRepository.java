package io.choerodon.devops.domain.application.repository;

import io.choerodon.devops.api.vo.iam.entity.DevopsEnvCommandValueE;

public interface DevopsEnvCommandValueRepository {

    DevopsEnvCommandValueE create(DevopsEnvCommandValueE devopsEnvCommandValueE);

    void deleteById(Long commandId);

    void updateValueById(Long valueId, String value);

}
