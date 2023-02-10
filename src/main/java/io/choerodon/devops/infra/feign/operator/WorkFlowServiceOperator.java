//package io.choerodon.devops.infra.feign.operator;
//
//import static io.choerodon.devops.infra.constant.ExceptionConstants.WorkflowCode.*;
//
//import io.reactivex.Emitter;
//import io.reactivex.Observable;
//import io.reactivex.ObservableOnSubscribe;
//import io.reactivex.Observer;
//import io.reactivex.disposables.Disposable;
//import io.reactivex.schedulers.Schedulers;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.ResponseEntity;
//import org.springframework.stereotype.Component;
//
//import io.choerodon.core.exception.CommonException;
//import io.choerodon.devops.api.vo.deploy.hzero.HzeroDeployPipelineVO;
//import io.choerodon.devops.infra.dto.workflow.DevopsPipelineDTO;
//import io.choerodon.devops.infra.feign.WorkFlowServiceClient;
//import io.choerodon.devops.infra.util.CustomContextUtil;
//
///**
// * Creator: ChangpingShi0213@gmail.com
// * Date:  17:11 2019/7/19
// * Description:
// */
//@Component
//public class WorkFlowServiceOperator {
//    @Autowired
//    private WorkFlowServiceClient workFlowServiceClient;
//
//    public Boolean approveUserTask(Long projectId, String businessKey) {
//        ResponseEntity<Boolean> responseEntity = workFlowServiceClient.approveUserTask(projectId, businessKey);
//        if (!responseEntity.getStatusCode().is2xxSuccessful()) {
//            throw new CommonException(DEVOPS_WORKFLOW_APPROVE);
//        }
//        return responseEntity.getBody();
//    }
//
//    public void approveUserTask(Long projectId, String businessKey, String loginName, Long userId, Long orgId) {
//        Observable.create((ObservableOnSubscribe<String>) Emitter::onComplete).subscribeOn(Schedulers.io())
//                .subscribe(new Observer<String>() {
//                    @Override
//                    public void onSubscribe(Disposable d) {
//                    }
//
//                    @Override
//                    public void onNext(String s) {
//                    }
//
//                    @Override
//                    public void onError(Throwable e) {
//                    }
//
//                    @Override
//                    public void onComplete() {
//                        CustomContextUtil.setUserContext(loginName, userId, orgId);
//                        try {
//                            approveUserTask(projectId, businessKey);
//                        } catch (Exception e) {
//                            throw new CommonException(e);
//                        }
//                    }
//                });
//    }
//
//
//    public void stopInstance(Long projectId, String businessKey) {
//        ResponseEntity<Void> responseEntity = workFlowServiceClient.stopInstance(projectId, businessKey);
//        if (!responseEntity.getStatusCode().is2xxSuccessful()) {
//            throw new CommonException(DEVOPS_WORKFLOW_STOP);
//        }
//    }
//
//    public String createCiCdPipeline(Long projectId, DevopsPipelineDTO devopsPipelineDTO) {
//        ResponseEntity<String> responseEntity = workFlowServiceClient.createCiCdPipeline(projectId, devopsPipelineDTO);
//        if (!responseEntity.getStatusCode().is2xxSuccessful()) {
//            throw new CommonException(DEVOPS_WORKFLOW_CREATE);
//        }
//        return responseEntity.getBody();
//    }
//
//    public String createHzeroPipeline(Long projectId, HzeroDeployPipelineVO hzeroDeployPipelineVO) {
//        ResponseEntity<String> responseEntity = workFlowServiceClient.createHzeroPipeline(projectId, hzeroDeployPipelineVO);
//        if (!responseEntity.getStatusCode().is2xxSuccessful()) {
//            throw new CommonException("devops.hzero.workflow.create");
//        }
//        return responseEntity.getBody();
//    }
//}
