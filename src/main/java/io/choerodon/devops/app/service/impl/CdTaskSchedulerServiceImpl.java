package io.choerodon.devops.app.service.impl;

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

import io.choerodon.devops.app.eventhandler.cd.AbstractCdJobHandler;
import io.choerodon.devops.app.eventhandler.cd.CdJobOperator;
import io.choerodon.devops.app.service.CdTaskSchedulerService;
import io.choerodon.devops.app.service.PipelineJobRecordService;
import io.choerodon.devops.app.service.PipelineLogService;
import io.choerodon.devops.infra.constant.GitOpsConstants;
import io.choerodon.devops.infra.dto.PipelineJobRecordDTO;
import io.choerodon.devops.infra.dto.PipelineLogDTO;
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
    PlatformTransactionManager transactionManager;
    @Autowired
    @Qualifier(value = GitOpsConstants.PIPELINE_EXEC_EXECUTOR)
    private AsyncTaskExecutor taskExecutor;
    @Autowired
    private PipelineLogService pipelineLogService;
    @Autowired
    private CdJobOperator cdJobOperator;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void schedulePeriodically() {
        // 查询数据库中处于pending状态的任务
        List<PipelineJobRecordDTO> pipelineJobRecordDTOS = pipelineJobRecordService.listPendingJobs(50);
        pipelineJobRecordDTOS.forEach(pipelineJobRecordDTO -> {
            Long jobRecordId = pipelineJobRecordDTO.getId();
            Long pipelineId = pipelineJobRecordDTO.getPipelineId();
            // 将待执行的任务状态置为running，并且加入线程池队列
            pipelineJobRecordService.updatePendingJobToRunning(jobRecordId);
            TransactionStatus transactionStatus = createTransactionStatus(transactionManager);
            taskExecutor.submit(() -> {
                // 设置线程上下文
                Long createdBy = pipelineJobRecordDTO.getCreatedBy();
                CustomContextUtil.setUserContext(createdBy);
                StringBuffer log = new StringBuffer();
                try {
                    // 执行job
                    AbstractCdJobHandler handler = cdJobOperator.getHandler(pipelineJobRecordDTO.getType());
                    handler.execCommand(jobRecordId, log);

                    // 更新任务状态为成功,记录日志
                    PipelineLogDTO pipelineLogDTO = pipelineLogService.saveLog(pipelineId, log.toString());

                } catch (Exception e) {
                    // 更新任务状态为失败,记录日志

                } finally {
                    // 记录job日志

                }
                SecurityContextHolder.clearContext();
                transactionManager.commit(transactionStatus);
                transactionManager.rollback(transactionStatus);
            });
        });

    }

    protected TransactionStatus createTransactionStatus(final PlatformTransactionManager transactionManager) {
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        return transactionManager.getTransaction(def);
    }
}
