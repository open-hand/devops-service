package io.choerodon.devops.domain.application.repository;

import com.github.pagehelper.PageInfo;

import java.util.List;

import io.choerodon.base.domain.PageRequest;
import io.choerodon.devops.api.vo.iam.entity.DevopsConfigMapE;

public interface DevopsConfigMapRepository {

    DevopsConfigMapE baseQueryByEnvIdAndName(Long envId, String name);

    DevopsConfigMapE baseCreate(DevopsConfigMapE devopsConfigMapE);

    DevopsConfigMapE baseUpdate(DevopsConfigMapE devopsConfigMapE);

    DevopsConfigMapE baseQueryById(Long id);

    void baseDelete(Long id);

    PageInfo<DevopsConfigMapE> basePageByEnv(Long envId, PageRequest pageRequest, String params, Long appId);

    List<DevopsConfigMapE> baseListByEnv(Long envId);

}
