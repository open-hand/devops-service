package io.choerodon.devops.domain.application.repository;

import java.util.List;

import io.choerodon.core.domain.Page;
import io.choerodon.devops.infra.dataobject.DevopsEnvPodContainerDO;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;

/**
 * Creator: Runge
 * Date: 2018/5/16
 * Time: 11:50
 * Description:
 */
public interface DevopsEnvPodContainerRepository {
    void insert(DevopsEnvPodContainerDO containerDO);

    void update(DevopsEnvPodContainerDO containerDO);

    void delete(Long id);

    void deleteByPodId(Long podId);

    DevopsEnvPodContainerDO get(Long id);

    DevopsEnvPodContainerDO get(DevopsEnvPodContainerDO container);

    List<DevopsEnvPodContainerDO> list(DevopsEnvPodContainerDO container);

    Page<DevopsEnvPodContainerDO> page(Long podId, PageRequest pageRequest, String param);
}
