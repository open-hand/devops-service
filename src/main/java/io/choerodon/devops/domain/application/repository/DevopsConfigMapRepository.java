package io.choerodon.devops.domain.application.repository;

import io.choerodon.core.domain.Page;
import io.choerodon.devops.domain.application.entity.DevopsConfigMapE;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;

public interface DevopsConfigMapRepository {

    DevopsConfigMapE queryByEnvIdAndName(Long envId, String name);

    DevopsConfigMapE create(DevopsConfigMapE devopsConfigMapE);

    DevopsConfigMapE update(DevopsConfigMapE devopsConfigMapE);

    DevopsConfigMapE queryById(Long id);

    void delete(Long id);

    Page<DevopsConfigMapE> listByEnv(Long envId, PageRequest pageRequest, String params);

}
