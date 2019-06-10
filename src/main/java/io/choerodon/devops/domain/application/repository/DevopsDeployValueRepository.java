package io.choerodon.devops.domain.application.repository;

import java.util.List;

import io.choerodon.core.domain.Page;
import io.choerodon.devops.domain.application.entity.DevopsDeployValueE;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  10:02 2019/4/10
 * Description:
 */
public interface DevopsDeployValueRepository {
    Page<DevopsDeployValueE> listByOptions(Long projectId, Long appId, Long envId, Long userId, PageRequest pageRequest, String params);

    DevopsDeployValueE createOrUpdate(DevopsDeployValueE pipelineRecordE);

    void delete(Long valueId);

    DevopsDeployValueE queryById(Long valueId);

    void checkName(Long projectId, String name);

    List<DevopsDeployValueE> queryByAppIdAndEnvId(Long projectId, Long appId, Long envId);
}
