package io.choerodon.devops.domain.application.repository;

import java.util.List;

import io.choerodon.devops.api.vo.iam.entity.DevopsEnvironmentE;

/**
 * Created by younger on 2018/4/9.
 */
public interface DevopsEnvironmentRepository {

    DevopsEnvironmentE baseCreate(DevopsEnvironmentE devopsEnvironmentE);

    DevopsEnvironmentE baseQueryById(Long id);

    Boolean baseUpdateActive(Long environmentId, Boolean active);

    DevopsEnvironmentE baseUpdate(DevopsEnvironmentE devopsEnvironmentE);

    void baseCheckCode(DevopsEnvironmentE devopsEnvironmentE);

    List<DevopsEnvironmentE> baseListByProjectId(Long projectId);

    List<DevopsEnvironmentE> baseListByProjectIdAndActive(Long projectId, Boolean active);

    DevopsEnvironmentE baseQueryByClusterIdAndCode(Long clusterId, String code);

    DevopsEnvironmentE baseQueryByProjectIdAndCode(Long projectId, String code);

    DevopsEnvironmentE baseQueryByToken(String token);

    List<DevopsEnvironmentE> baseListAll();

    void baseUpdateSagaSyncEnvCommit(DevopsEnvironmentE devopsEnvironmentE);

    void baseUpdateDevopsSyncEnvCommit(DevopsEnvironmentE devopsEnvironmentE);

    void baseUpdateAgentSyncEnvCommit(DevopsEnvironmentE devopsEnvironmentE);


    void baseDeleteById(Long id);

<<<<<<< HEAD
    List<DevopsEnvironmentE> listByClusterId(Long clusterId);

    void updateOptions(DevopsEnvironmentE devopsEnvironmentE);
=======
    List<DevopsEnvironmentE> baseListByClusterId(Long clusterId);
>>>>>>> [IMP] 重构Repository
}
