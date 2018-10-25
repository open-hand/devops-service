package io.choerodon.devops.domain.application.repository;

import java.util.List;

import io.choerodon.core.domain.Page;
import io.choerodon.devops.api.dto.EnvUserPermissionDTO;
import io.choerodon.devops.domain.application.entity.DevopsEnvironmentE;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;

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

    DevopsEnvironmentE queryByProjectIdAndCode(Long projectId, String code);

    DevopsEnvironmentE queryByToken(String token);

    List<DevopsEnvironmentE> list();

    void updateEnvCommit(DevopsEnvironmentE devopsEnvironmentE);

    Page<EnvUserPermissionDTO> pageUserPermission(Long envId, PageRequest pageRequest);
}
