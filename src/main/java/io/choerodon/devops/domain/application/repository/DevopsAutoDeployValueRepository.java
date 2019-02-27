package io.choerodon.devops.domain.application.repository;

import io.choerodon.devops.domain.application.entity.DevopsAutoDeployValueE;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  16:11 2019/2/26
 * Description:
 */
public interface DevopsAutoDeployValueRepository {
    Long createOrUpdate(DevopsAutoDeployValueE devopsAutoDeployValueE);
}
