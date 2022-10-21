package io.choerodon.devops.app.service;

import io.choerodon.devops.infra.dto.DevopsProjectDTO;

public interface ProjectService {

    /**
     * 根据Id查询project
     *
     * @param projectId
     * @return
     */
    DevopsProjectDTO queryById(Long projectId);

}
