package io.choerodon.devops.app.service.impl;

import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.util.CollectionUtils;

import io.choerodon.asgard.saga.producer.StartSagaBuilder;
import io.choerodon.asgard.saga.producer.TransactionalProducer;
import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.devops.api.vo.cd.PipelineJobFinishVO;
import io.choerodon.devops.app.eventhandler.cd.AbstractCdJobHandler;
import io.choerodon.devops.app.eventhandler.cd.CdJobOperator;
import io.choerodon.devops.app.eventhandler.constants.SagaTopicCodeConstants;
import io.choerodon.devops.app.service.CdTaskSchedulerService;
import io.choerodon.devops.app.service.PipelineJobRecordService;
import io.choerodon.devops.app.service.PipelineLogService;
import io.choerodon.devops.app.service.PipelineStageRecordService;
import io.choerodon.devops.infra.constant.GitOpsConstants;
import io.choerodon.devops.infra.dto.PipelineJobRecordDTO;
import io.choerodon.devops.infra.enums.cd.PipelineStatusEnum;
import io.choerodon.devops.infra.util.CustomContextUtil;
import io.choerodon.devops.infra.util.JsonHelper;
import io.choerodon.devops.infra.util.LogUtil;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2022/11/23 17:26
 */
@Service
public class CdTaskSchedulerServiceImpl implements CdTaskSchedulerService {

    private static final Logger LOGGER = LoggerFactory.getLogger(CdTaskSchedulerServiceImpl.class);
    @Autowired
    private PipelineJobRecordService pipelineJobRecordService;
    @Autowired
    private PipelineStageRecordService pipelineStageRecordService;
    @Autowired
    private PipelineLogService pipelineLogService;

    @Autowired
    PlatformTransactionManager transactionManager;
    @Autowired
    @Qualifier(value = GitOpsConstants.PIPELINE_EXEC_EXECUTOR)
    private AsyncTaskExecutor taskExecutor;
    @Autowired
    private CdJobOperator cdJobOperator;

    @Autowired
    private TransactionalProducer producer;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void schedulePeriodically() {
        // 查询数据库中处于pending状态的任务
        List<PipelineJobRecordDTO> pipelineJobRecordDTOS = pipelineJobRecordService.listPendingJobs(50);
        pipelineJobRecordDTOS.forEach(pipelineJobRecordDTO -> {
            Long jobRecordId = pipelineJobRecordDTO.getId();
            Long stageRecordId = pipelineJobRecordDTO.getStageRecordId();
            Long pipelineId = pipelineJobRecordDTO.getPipelineId();
            Long projectId = pipelineJobRecordDTO.getProjectId();

            // 将待执行的任务状态置为running，并且加入线程池队列
            if (pipelineJobRecordService.updatePendingJobToRunning(jobRecordId) == 1) {
                taskExecutor.submit(() -> {
                    TransactionStatus transactionStatus = createTransactionStatus(transactionManager);
                    try {
                        // 设置线程上下文
                        Long createdBy = pipelineJobRecordDTO.getCreatedBy();
                        CustomContextUtil.setUserContext(createdBy);
                        StringBuilder log = new StringBuilder();

                        try {
                            // 执行job
                            AbstractCdJobHandler handler = cdJobOperator.getHandler(pipelineJobRecordDTO.getType());
                            handler.execCommand(jobRecordId, log);
                        } catch (Exception e) {
                            LOGGER.error("Execute job failed.", e);
                            log.append(LogUtil.readContentOfThrowable(e));
                            // 更新任务状态为失败
                            pipelineJobRecordService.updateStatus(jobRecordId, PipelineStatusEnum.FAILED);

                        }
                        // 记录job日志
                        pipelineLogService.saveLog(pipelineId, jobRecordId, log.toString());
                        // 更新阶段状态
                        producer.apply(
                                StartSagaBuilder
                                        .newBuilder()
                                        .withLevel(ResourceLevel.PROJECT)
                                        .withSourceId(projectId)
                                        .withRefType("update-stage-status")
                                        .withRefId(stageRecordId.toString())
                                        .withSagaCode(SagaTopicCodeConstants.DEVOPS_PIPELINE_JOB_FINISH),
                                builder -> builder
                                        .withJson(JsonHelper.marshalByJackson(new PipelineJobFinishVO(stageRecordId, jobRecordId)))
                                        .withRefId(jobRecordId.toString()));
                        transactionManager.commit(transactionStatus);
                    } catch (Exception e) {
                        LOGGER.error("Update job status failed.", e);
                        transactionManager.rollback(transactionStatus);
                    } finally {
                        SecurityContextHolder.clearContext();
                    }
                });
            }

        });

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cleanTimeoutTask(Long timeoutDuration) {
        Date date = new Date(System.currentTimeMillis() - timeoutDuration);
        List<PipelineJobRecordDTO> pipelineJobRecordDTOS = pipelineJobRecordService.listRunningTaskBeforeDate(date);
        if (!CollectionUtils.isEmpty(pipelineJobRecordDTOS)) {
            pipelineJobRecordDTOS.forEach(pipelineJobRecordDTO -> {
                // 更新任务状态为失败
                pipelineJobRecordDTO.setStatus(PipelineStatusEnum.FAILED.value());
                pipelineJobRecordService.update(pipelineJobRecordDTO);
                // 保存执行日志
                pipelineLogService.saveLog(pipelineJobRecordDTO.getPipelineId(), pipelineJobRecordDTO.getId(), "Execute this task timeout!");
                pipelineStageRecordService.updateStatus(pipelineJobRecordDTO.getStageRecordId());
            });
        }
    }

    protected TransactionStatus createTransactionStatus(final PlatformTransactionManager transactionManager) {
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        return transactionManager.getTransaction(def);
    }
}
