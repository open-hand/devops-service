package io.choerodon.devops.domain.application.repository;

import com.github.pagehelper.PageInfo;

import java.util.List;

import io.choerodon.base.domain.PageRequest;
import io.choerodon.devops.domain.application.entity.DevopsConfigMapE;

public interface DevopsConfigMapRepository {

    DevopsConfigMapE queryByEnvIdAndName(Long envId, String name);

    DevopsConfigMapE create(DevopsConfigMapE devopsConfigMapE);

    DevopsConfigMapE update(DevopsConfigMapE devopsConfigMapE);

    DevopsConfigMapE queryById(Long id);

    void delete(Long id);

    PageInfo<DevopsConfigMapE> pageByEnv(Long envId, PageRequest pageRequest, String params);

    PageInfo<DevopsConfigMapE> pageByApp(List<Long> configMapIds, PageRequest pageRequest, String params);

    List<DevopsConfigMapE> listByEnv(Long envId);

}
