package io.choerodon.devops.domain.application.repository;

import io.choerodon.devops.infra.dataobject.workflow.DevopsPipelineDTO;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  19:52 2019/4/9
 * Description:
 */
public interface WorkFlowRepository {
    String create(Long projectId, DevopsPipelineDTO devopsPipelineDTO);

    Boolean approveUserTask(Long projectId, String instanceId, Boolean isApprove);
}
