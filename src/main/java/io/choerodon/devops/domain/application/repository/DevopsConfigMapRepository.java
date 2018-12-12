package io.choerodon.devops.domain.application.repository;

import java.util.List;

import io.choerodon.core.domain.Page;
import io.choerodon.devops.domain.application.entity.DevopsConfigMapE;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;

public interface DevopsConfigMapRepository {

    DevopsConfigMapE queryByEnvIdAndName(Long envId, String name);

    DevopsConfigMapE create(DevopsConfigMapE devopsConfigMapE);

    DevopsConfigMapE update(DevopsConfigMapE devopsConfigMapE);

    DevopsConfigMapE queryById(Long id);

    void delete(Long id);

    Page<DevopsConfigMapE> pageByEnv(Long envId, PageRequest pageRequest, String params);

    List<DevopsConfigMapE> listByEnv(Long envId);

}
