package io.choerodon.devops.domain.application.repository;

import java.util.List;

import io.choerodon.devops.domain.application.entity.DevopsEnvGroupE;

public interface DevopsEnvGroupRepository {


    DevopsEnvGroupE create(DevopsEnvGroupE devopsEnvGroupE);

    DevopsEnvGroupE update(DevopsEnvGroupE devopsEnvGroupE);

    List<DevopsEnvGroupE> listByProjectId(Long projectId);

    DevopsEnvGroupE queryByProjectIdAndName(String name, Long projectId);

    DevopsEnvGroupE query(Long id);
}
