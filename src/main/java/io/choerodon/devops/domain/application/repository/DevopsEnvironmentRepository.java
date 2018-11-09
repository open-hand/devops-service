package io.choerodon.devops.domain.application.repository;

import java.util.List;

import io.choerodon.devops.domain.application.entity.DevopsEnvironmentE;

/**
 * Created by younger on 2018/4/9.
 */
public interface DevopsEnvironmentRepository {

    DevopsEnvironmentE create(DevopsEnvironmentE devopsEnvironmentE);

    DevopsEnvironmentE queryById(Long id);

    Boolean activeEnvironment(Long environmentId, Boolean active);

    DevopsEnvironmentE update(DevopsEnvironmentE devopsEnvironmentE);

    void checkName(DevopsEnvironmentE devopsEnvironmentE);

    void checkCode(DevopsEnvironmentE devopsEnvironmentE);

    List<DevopsEnvironmentE> queryByProject(Long projectId);

    List<DevopsEnvironmentE> queryByprojectAndActive(Long projectId, Boolean active);

    DevopsEnvironmentE queryByClusterIdAndCode(Long projectId, String code);

    DevopsEnvironmentE queryByToken(String token);

    List<DevopsEnvironmentE> list();

    void updateEnvCommit(DevopsEnvironmentE devopsEnvironmentE);

    void deleteById(Long id);

    List<DevopsEnvironmentE> listByClusterId(Long clusterId);

    void deleteById(Long id);
}
