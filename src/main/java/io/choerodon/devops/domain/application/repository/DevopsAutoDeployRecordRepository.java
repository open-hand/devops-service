package io.choerodon.devops.domain.application.repository;

import io.choerodon.core.domain.Page;
import io.choerodon.devops.domain.application.entity.DevopsAutoDeployRecordE;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  19:29 2019/2/26
 * Description:
 */
public interface DevopsAutoDeployRecordRepository {

    Page<DevopsAutoDeployRecordE> listByOptions(Long projectId,
                                                Long appId,
                                                Long envId,
                                                String taskName,
                                                Boolean doPage,
                                                PageRequest pageRequest,
                                                String params);

    void updateStatus(Long autoDeployId, String status);
}
