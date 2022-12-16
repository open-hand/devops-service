//package io.choerodon.devops.app.service;
//
//import javax.annotation.Nullable;
//
//import io.choerodon.devops.api.vo.*;
//import io.choerodon.devops.infra.dto.CiCdPipelineDTO;
//
//public interface DevopsCdPipelineService {
//
////    /**
////     * 处理ci流水线状态变更
////     * ci流水线状态为pendding、running,计算cd要执行的阶段、任务，并更新cd流水线状态为未开始
////     * ci流水线状态为success， 执行cd流水线
////     *
////     * @param pipelineWebHookVO ci流水线记录信息
////     */
////    void handleCiPipelineStatusUpdate(PipelineWebHookVO pipelineWebHookVO);
//
////    void triggerCdPipeline(Long projectId, String token, String commit, String ref, Boolean tag, Long gitlabPipelineId);
//
//    void executeCdPipeline(Long pipelineRecordId);
//
//
//    /**
//     * 执行环境部署任务
//     *
//     * @param pipelineRecordId
//     * @param stageRecordId
//     * @param jobRecordId
//     */
//    void envAutoDeploy(Long pipelineRecordId, Long stageRecordId, Long jobRecordId);
//
//
//    void pipelineDeploy(Long pipelineRecordId, Long stageRecordId, Long jobRecordId, StringBuilder log);
//
//    void pipelineDeployDeployment(Long pipelineRecordId, Long stageRecordId, Long jobRecordId, StringBuilder log);
//    /**
//     * 执行下一个任务
//     * 1. 人工卡点任务更新为待审核
//     * 2. 当前阶段结束，则执行下一个阶段
//     * 3. 如果是，最后一个阶段的最后一个任务，则更新流水线状态为success
//     *
//     * @param pipelineRecordId
//     * @param stageRecordId
//     * @param jobRecordId
//     * @param status
//     */
//    void setAppDeployStatus(Long pipelineRecordId, Long stageRecordId, Long jobRecordId, Boolean status);
//
//    /**
//     * 查询Job状态
//     *
//     * @param pipelineRecordId
//     * @param stageRecordId
//     * @param jobRecordId
//     * @return
//     */
//    String getDeployStatus(Long pipelineRecordId, Long stageRecordId, Long jobRecordId);
//
//    void createWorkFlow(Long projectId, io.choerodon.devops.infra.dto.workflow.DevopsPipelineDTO devopsPipelineDTO, String loginName, Long userId, Long orgId);
//
//    AuditResultVO auditJob(Long projectId, Long pipelineRecordId, Long stageRecordId, Long jobRecordId, String result);
//
//    AduitStatusChangeVO checkAuditStatus(Long projectId, Long pipelineRecordId, AuditCheckVO auditCheckVO);
//
////    void handlerCiPipelineStatusSuccess(PipelineWebHookVO pipelineWebHookVO, String token);
//
////    void trigerSimpleCDPipeline(PipelineWebHookVO pipelineWebHookVO);
//
////    void initPipelineRecordWithStageAndJob(Long projectId, Long gitlabPipelineId, String commitSha, String ref, Boolean tag, CiCdPipelineDTO devopsCiPipelineDTO);
//
//    /**
//     * 执行api测试任务
//     *
//     * @param pipelineRecordId
//     * @param stageRecordId
//     * @param jobRecordId
//     */
//    void executeApiTestTask(Long pipelineRecordId, Long stageRecordId, Long jobRecordId);
//
//    /**
//     * 查询部署任务的部署结果
//     *
//     * @param pipelineRecordId
//     * @param deployJobName
//     * @return
//     */
//    String getDeployStatus(Long pipelineRecordId, String deployJobName);
//
////    /**
////     * 执行外部卡点任务
////     *
////     * @param pipelineRecordId
////     * @param stageRecordId
////     * @param jobRecordId
////     */
////    void executeExternalApprovalTask(Long pipelineRecordId, Long stageRecordId, Long jobRecordId);
////
////    /**
////     * 外部卡点任务回调接口，用于接收审批结果
////     *
////     * @param pipelineRecordId
////     * @param stageRecordId
////     * @param jobRecordId
////     * @param callbackToken    回调时用于认证的token
////     * @param status           审批结果 true,false
////     */
////    void externalApprovalTaskCallback(Long pipelineRecordId, Long stageRecordId, Long jobRecordId, String callbackToken, Boolean status);
////
////    /**
////     * 查询外部卡点任务回调接口地址
////     *
////     * @return
////     */
////    String queryCallbackUrl();
//
//    /**
//     * 查询引用了实例作为替换对象的流水线信息，如果有多个任务引用了这个实例，取一个
//     *
//     * @param projectId  项目id
//     * @param instanceId 实例id
//     * @return 一个或者无
//     */
//    @Nullable
//    PipelineInstanceReferenceVO queryPipelineReference(Long projectId, Long instanceId);
//
//    /**
//     * 查询引用了环境应用作为替换对象的流水线信息，如果有多个任务引用了这个实例，取一个
//     *
//     * @param projectId  项目id
//     * @param appId 应用id
//     * @return 一个或者无
//     */
//    @Nullable
//    PipelineInstanceReferenceVO queryPipelineReferenceEnvApp(Long projectId, Long appId);
//
//    /**
//     * 查询引用了主机应用作为替换对象的流水线信息，如果有多个任务引用了这个实例，取一个
//     *
//     * @param projectId  项目id
//     * @param appId 应用id
//     * @return 一个或者无
//     */
//    @Nullable
//    PipelineInstanceReferenceVO queryPipelineReferenceHostApp(Long projectId, Long appId);
//
//    void hostDeployStatusUpdate(Long commandId, Long jobRecordId, Boolean status, String error);
//}
