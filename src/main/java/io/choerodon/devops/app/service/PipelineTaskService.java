package io.choerodon.devops.app.service;

import io.choerodon.devops.api.dto.PipelineAppDeployDTO;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  9:25 2019/4/4
 * Description:
 */
public interface PipelineTaskService {
    PipelineAppDeployDTO createAppDeploy(Long projectId, PipelineAppDeployDTO appDeployDTO);
}
