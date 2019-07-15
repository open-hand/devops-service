package io.choerodon.devops.domain.application.repository;

import java.util.List;

import io.choerodon.devops.api.vo.iam.entity.DevopsEnvGroupE;

public interface DevopsEnvGroupRepository {

    DevopsEnvGroupE baseCreate(DevopsEnvGroupE devopsEnvGroupE);

    DevopsEnvGroupE baseUpdate(DevopsEnvGroupE devopsEnvGroupE);

    List<DevopsEnvGroupE> baseListByProjectId(Long projectId);

    DevopsEnvGroupE baseQuery(Long id);

    Boolean baseCheckUniqueInProject(Long id, String name, Long projectId);

    Boolean baseCheckUniqueInProject(String name, Long projectId);

    void baseDelete(Long id);
}
