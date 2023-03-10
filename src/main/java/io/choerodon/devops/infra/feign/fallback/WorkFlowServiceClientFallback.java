//package io.choerodon.devops.infra.feign.fallback;
//
//import static io.choerodon.devops.infra.constant.ExceptionConstants.WorkflowCode.*;
//
//import org.springframework.http.ResponseEntity;
//import org.springframework.stereotype.Component;
//
//import io.choerodon.core.exception.CommonException;
//import io.choerodon.devops.api.vo.deploy.hzero.HzeroDeployPipelineVO;
//import io.choerodon.devops.infra.dto.workflow.DevopsPipelineDTO;
//import io.choerodon.devops.infra.feign.WorkFlowServiceClient;
//
///**
// * Creator: ChangpingShi0213@gmail.com
// * Date:  19:40 2019/4/9
// * Description:
// */
//@Component
//public class WorkFlowServiceClientFallback implements WorkFlowServiceClient {
//
//    @Override
//    public ResponseEntity<String> create(Long projectId, DevopsPipelineDTO devopsPipelineDTO) {
//        throw new CommonException(DEVOPS_WORKFLOW_CREATE);
//    }
//    @Override
//    public ResponseEntity<Boolean> approveUserTask(Long projectId, String businessKey) {
//        throw new CommonException(DEVOPS_WORKFLOW_APPROVE);
//    }
//
//    @Override
//    public ResponseEntity stopInstance(Long projectId, String businessKey) {
//        throw new CommonException(DEVOPS_WORKFLOW_STOP);
//    }
//
//    @Override
//    public ResponseEntity<String> createCiCdPipeline(Long projectId, DevopsPipelineDTO devopsPipelineDTO) {
//        throw new CommonException(DEVOPS_WORKFLOW_CREATE);
//    }
//
//    @Override
//    public ResponseEntity<String> createHzeroPipeline(Long projectId, HzeroDeployPipelineVO hzeroDeployPipelineVO) {
//        throw new CommonException("devops.hzero.deploy.create");
//    }
//}
