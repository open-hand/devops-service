package io.choerodon.devops.domain.application.repository;

import io.choerodon.devops.domain.application.entity.DevopsAppResourceE;

import java.util.List;

/**
 * @author lizongwei
 * @date 2019/7/3
 */
public interface DevopsAppResourceRepository {

    void insert(DevopsAppResourceE devopsAppResourceE);

    void deleteByAppIdAndType(Long appId, String type);

    List<DevopsAppResourceE> queryByAppAndType(Long appId, String type);
}
