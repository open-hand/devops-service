package io.choerodon.devops.domain.application.repository;

import io.choerodon.core.domain.Page;
import io.choerodon.devops.domain.application.entity.DevopsAutoDeployE;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;

import java.util.List;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  16:11 2019/2/26
 * Description:
 */
public interface DevopsAutoDeployRepository {
    void checkTaskName(Long id, Long projectId, String taskName);

    DevopsAutoDeployE createOrUpdate(DevopsAutoDeployE devopsAutoDeployE);

    void delete(Long autoDeployId);

    Page<DevopsAutoDeployE> listByOptions(Long projectId,
                                          Long appId,
                                          Long envId,
                                          Boolean doPage,
                                          PageRequest pageRequest,
                                          String params);

    List<DevopsAutoDeployE> queryByProjectId(Long projectId);

    DevopsAutoDeployE queryById(Long autoDeployId);

    DevopsAutoDeployE updateIsEnabled(Long autoDeployId, Integer isEnabled);

    List<DevopsAutoDeployE> queryByVersion(Long appId, String branch);


    void updateInstanceId(Long instanceId);
}
