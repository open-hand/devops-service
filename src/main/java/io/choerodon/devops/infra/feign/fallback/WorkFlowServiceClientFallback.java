package io.choerodon.devops.infra.feign.fallback;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.infra.dto.workflow.DevopsPipelineDTO;
import io.choerodon.devops.infra.feign.WorkFlowServiceClient;
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
        throw new CommonException("error.workflow.create");
    }

    @Override
    public ResponseEntity<Boolean> approveUserTask(Long projectId, String business_key) {
        throw new CommonException("error.workflow.approve");
    }

    @Override
    public ResponseEntity stopInstance(Long projectId, String business_key) {
        throw new CommonException("error.workflow.stop");
    }
}
