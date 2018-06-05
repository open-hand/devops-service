package io.choerodon.devops.domain.application.repository;

import java.util.List;

import io.choerodon.devops.domain.application.entity.DevopsEnvResourceE;

/**
 * Created by younger on 2018/4/24.
 */
public interface DevopsEnvResourceRepository {

    void create(DevopsEnvResourceE devopsEnvResourceE);

    List<DevopsEnvResourceE> listByInstanceId(Long instanceId);

    List<DevopsEnvResourceE> listJobByInstanceId(Long instanceId);

    DevopsEnvResourceE queryByInstanceIdAndKindAndName(Long instanceId, String kind, String name);

    void update(DevopsEnvResourceE devopsEnvResourceE);

    void deleteByKindAndName(String kind, String name);

    List<DevopsEnvResourceE> listByEnvAndType(Long envId, String type);

}