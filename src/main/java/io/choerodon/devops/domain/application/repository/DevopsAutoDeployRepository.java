package io.choerodon.devops.domain.application.repository;

import io.choerodon.core.domain.Page;
import io.choerodon.devops.domain.application.entity.DevopsAutoDeployE;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  16:11 2019/2/26
 * Description:
 */
public interface DevopsAutoDeployRepository {
    void checkTaskName(Long projectId, String taskName);

    DevopsAutoDeployE createOrUpdate(DevopsAutoDeployE devopsAutoDeployE);

    void delete(Long autoDeployId);

    Page<DevopsAutoDeployE> listByOptions(Long projectId,
                                          Long appId,
                                          Long envId,
                                          Boolean doPage,
                                          PageRequest pageRequest,
                                          String params);
}
