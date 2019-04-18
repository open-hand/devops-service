package io.choerodon.devops.infra.feign.fallback;

import io.choerodon.devops.infra.dataobject.workflow.DevopsPipelineDTO;
import io.choerodon.devops.infra.feign.WorkFlowServiceClient;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  19:40 2019/4/9
 * Description:
 */
@Component
public class WorkFlowServiceClientFallback implements WorkFlowServiceClient {
    @Override
    public ResponseEntity<String> create(Long projectId, DevopsPipelineDTO devopsPipelineDTO) {
        return new ResponseEntity("error.workflow.create", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<Boolean> approveUserTask(Long projectId, String processInstanceId, Boolean isApprove) {
        return new ResponseEntity("error.workflow.approve", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity approveUserTask(Long projectId, String processInstanceId) {
        return new ResponseEntity("error.workflow.stop", HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
