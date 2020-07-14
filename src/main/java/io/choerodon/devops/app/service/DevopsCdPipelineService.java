package io.choerodon.devops.app.service;

import io.choerodon.devops.api.vo.AduitStatusChangeVO;
import io.choerodon.devops.api.vo.AuditCheckVO;
import io.choerodon.devops.api.vo.AuditResultVO;
import io.choerodon.devops.api.vo.PipelineWebHookVO;

public interface DevopsCdPipelineService {

    /**
     * 处理ci流水线状态变更
     * ci流水线状态为pendding、running,计算cd要执行的阶段、任务，并更新cd流水线状态为未开始
     * ci流水线状态为success， 执行cd流水线
     *
     * @param pipelineWebHookVO ci流水线记录信息
     */
    void handleCiPipelineStatusUpdate(PipelineWebHookVO pipelineWebHookVO);

    void triggerCdPipeline(String token, String commit, String ref, Long gitlabPipelineId);

    void executeCdPipeline(Long pipelineRecordId);


    /**
     * 执行环境部署任务
     *
     * @param pipelineRecordId
     * @param stageRecordId
     * @param jobRecordId
     */
    void envAutoDeploy(Long pipelineRecordId, Long stageRecordId, Long jobRecordId);

    /**
     * 执行下一个任务
     * 1. 人工卡点任务更新为待审核
     * 2. 当前阶段结束，则执行下一个阶段
     * 3. 如果是，最后一个阶段的最后一个任务，则更新流水线状态为success
     *
     * @param pipelineRecordId
     * @param stageRecordId
     * @param jobRecordId
     * @param status
     */
    void setAppDeployStatus(Long pipelineRecordId, Long stageRecordId, Long jobRecordId, Boolean status);

    /**
     * 查询Job状态
     *
     * @param pipelineRecordId
     * @param stageRecordId
     * @param jobRecordId
     * @return
     */
    String getDeployStatus(Long pipelineRecordId, Long stageRecordId, Long jobRecordId);

    /**
     * 审核阶段
     *
     * @param projectId
     * @param pipelineRecordId
     * @param stageRecordId
     * @param result
     */
    AuditResultVO auditStage(Long projectId, Long pipelineRecordId, Long stageRecordId, String result);

    void createWorkFlow(Long projectId, io.choerodon.devops.infra.dto.workflow.DevopsPipelineDTO devopsPipelineDTO, String loginName, Long userId, Long orgId);

    AuditResultVO auditJob(Long projectId, Long pipelineRecordId, Long stageRecordId, Long jobRecordId, String result);

    AduitStatusChangeVO checkAuditStatus(Long projectId, Long pipelineRecordId, AuditCheckVO auditCheckVO);

    void handlerCiPipelineStatusSuccess(PipelineWebHookVO pipelineWebHookVO, String token);

    void trigerSimpleCDPipeline(PipelineWebHookVO pipelineWebHookVO);

}
