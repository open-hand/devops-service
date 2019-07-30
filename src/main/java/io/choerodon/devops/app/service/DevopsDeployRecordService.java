package io.choerodon.devops.app.service;


import io.choerodon.base.domain.PageRequest;
import io.choerodon.devops.infra.dto.DevopsDeployRecordDTO;

/**
 * Created by Sheep on 2019/7/29.
 */
public interface DevopsDeployRecordService {

    DevopsDeployRecordDTO basePageByProjectId(Long projectId, PageRequest pageRequest);

}
