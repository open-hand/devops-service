package io.choerodon.devops.domain.application.repository;


import java.util.List;

import com.github.pagehelper.PageInfo;

import io.choerodon.base.domain.PageRequest;
import io.choerodon.devops.api.vo.iam.entity.DevopsDeployValueE;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  10:02 2019/4/10
 * Description:
 */
public interface DevopsDeployValueRepository {

    PageInfo<DevopsDeployValueE> baseListByOptions(Long projectId, Long appId, Long envId, Long userId, PageRequest pageRequest, String params);

    DevopsDeployValueE baseCreateOrUpdate(DevopsDeployValueE pipelineRecordE);

    void baseDelete(Long valueId);

    DevopsDeployValueE baseQueryById(Long valueId);

    void baseCheckName(Long projectId, String name);

    List<DevopsDeployValueE> baseQueryByAppIdAndEnvId(Long projectId, Long appId, Long envId);
}
