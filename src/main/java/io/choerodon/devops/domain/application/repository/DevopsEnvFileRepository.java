package io.choerodon.devops.domain.application.repository;

import java.util.List;

import io.choerodon.devops.domain.application.entity.DevopsEnvFileE;

public interface DevopsEnvFileRepository {

    DevopsEnvFileE create(DevopsEnvFileE devopsEnvFileE);

    List<DevopsEnvFileE> listByEnvId(Long envId);

    DevopsEnvFileE queryByEnvAndPath(Long envId, String path);

    void update(DevopsEnvFileE devopsEnvFileE);

    void delete(DevopsEnvFileE devopsEnvFileE);
}
