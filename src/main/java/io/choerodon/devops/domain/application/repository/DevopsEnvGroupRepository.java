package io.choerodon.devops.domain.application.repository;

import java.util.List;

public interface DevopsEnvGroupRepository {

    DevopsEnvGroupE baseCreate(DevopsEnvGroupE devopsEnvGroupE);

    DevopsEnvGroupE baseUpdate(DevopsEnvGroupE devopsEnvGroupE);

    List<DevopsEnvGroupE> baseListByProjectId(Long projectId);

    DevopsEnvGroupE baseQuery(Long id);

    Boolean baseCheckUniqueInProject(Long id, String name, Long projectId);

    Boolean baseCheckUniqueInProject(String name, Long projectId);

    void baseDelete(Long id);
}
