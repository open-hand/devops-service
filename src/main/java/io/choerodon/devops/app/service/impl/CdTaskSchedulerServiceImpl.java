package io.choerodon.devops.app.service.impl;

import java.util.Date;
import java.util.List;

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

import io.choerodon.devops.app.eventhandler.cd.AbstractCdJobHandler;
import io.choerodon.devops.app.eventhandler.cd.CdJobOperator;
import io.choerodon.devops.app.service.*;
import io.choerodon.devops.infra.constant.GitOpsConstants;
import io.choerodon.devops.infra.dto.PipelineJobRecordDTO;
import io.choerodon.devops.infra.enums.cd.PipelineStatusEnum;
import io.choerodon.devops.infra.util.CustomContextUtil;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2022/11/23 17:26
 */
@Service
public class CdTaskSchedulerServiceImpl implements CdTaskSchedulerService {

    @Autowired
    private PipelineJobRecordService pipelineJobRecordService;
    @Autowired
    private PipelineStageRecordService pipelineStageRecordService;
    @Autowired
    private PipelineRecordService pipelineRecordService;
    @Autowired
    private PipelineLogService pipelineLogService;

    @Autowired
    PlatformTransactionManager transactionManager;
    @Autowired
    @Qualifier(value = GitOpsConstants.PIPELINE_EXEC_EXECUTOR)
    private AsyncTaskExecutor taskExecutor;
    @Autowired
    private CdJobOperator cdJobOperator;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void schedulePeriodically() {
        // 查询数据库中处于pending状态的任务
        List<PipelineJobRecordDTO> pipelineJobRecordDTOS = pipelineJobRecordService.listPendingJobs(50);
        pipelineJobRecordDTOS.forEach(pipelineJobRecordDTO -> {
            Long jobRecordId = pipelineJobRecordDTO.getId();
            Long stageRecordId = pipelineJobRecordDTO.getStageRecordId();
            Long pipelineId = pipelineJobRecordDTO.getPipelineId();

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
                            // 更新任务状态为失败
                            pipelineJobRecordService.updateStatus(jobRecordId, PipelineStatusEnum.FAILED);
                            // 更新阶段状态
                            pipelineStageRecordService.updateStatus(stageRecordId);
                        }
                        // 记录job日志
                        pipelineLogService.saveLog(pipelineId, jobRecordId, log.toString());
                        transactionManager.commit(transactionStatus);
                    } catch (Exception e) {
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
