package io.choerodon.devops.domain.application.repository;

import java.util.List;

import io.choerodon.devops.infra.dataobject.DevopsEnvPodDO;

/**
 * Creator: Runge
 * Date: 2018/4/17
 * Time: 13:52
 * Description:
 */
public interface DeployDetailRepository {
    List<DevopsEnvPodDO> getPods(Long instanceId);
}
