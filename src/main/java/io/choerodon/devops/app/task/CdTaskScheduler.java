package io.choerodon.devops.app.task;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import io.choerodon.asgard.schedule.annotation.JobParam;
import io.choerodon.asgard.schedule.annotation.JobTask;
import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.devops.app.service.CdTaskSchedulerService;
import io.choerodon.devops.app.service.PipelineScheduleService;
import io.choerodon.devops.app.service.PipelineService;
import io.choerodon.devops.infra.constant.MiscConstants;
import io.choerodon.devops.infra.dto.PipelineScheduleDTO;
import io.choerodon.devops.infra.enums.cd.PipelineTriggerTypeEnum;
import io.choerodon.devops.infra.util.CustomContextUtil;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2022/11/23 17:22
 */
@ConditionalOnProperty(value = "local.test", havingValue = "false", matchIfMissing = true)
@Component
@EnableScheduling
public class CdTaskScheduler {

    private static final Logger LOGGER = LoggerFactory.getLogger(CdTaskScheduler.class);

    @Value("${devops.pipeline.task.timeoutDuration:600000}")
    private Long timeoutDuration;
    @Autowired
    private CdTaskSchedulerService cdTaskSchedulerService;
    @Autowired
    private PipelineService pipelineService;
    @Autowired
    private PipelineScheduleService pipelineScheduleService;


    /**
     * 每5s执行一次
     */
    @Scheduled(cron = "0/5 * * * * ?")
    public void addTasks() {
        cdTaskSchedulerService.schedulePeriodically();
    }

    /**
     * 5分钟执行一次，将running状态超时的任务改为failed
     */
    @Scheduled(fixedRate = 1000 * 60 * 5)
    public void cleanTimeoutTask() {
        cdTaskSchedulerService.cleanTimeoutTask(timeoutDuration);
    }

    @JobTask(maxRetryCount = 3,
            code = MiscConstants.PIPELINE_SCHEDULE_TRIGGER,
            level = ResourceLevel.PROJECT,
            description = "流水线定时触发任务", params = {
            @JobParam(name = MiscConstants.PROJECT_ID, type = Long.class, defaultValue = "0", description = "项目id"),
            @JobParam(name = MiscConstants.PIPELINE_ID, type = Long.class, defaultValue = "0", description = "流水线id"),
            @JobParam(name = MiscConstants.SCHEDULE_TOKEN, description = "流水线id"),
            @JobParam(name = MiscConstants.USER_ID, type = Long.class, defaultValue = "0", description = "流水线id")
    })
    public void pipelineScheduleTrigger(Map<String, Object> map) {
        Long pipelineId = (Long) map.get(MiscConstants.PIPELINE_ID);
        String scheduleToken = map.get(MiscConstants.SCHEDULE_TOKEN).toString();
        Long userId = (Long) map.get(MiscConstants.USER_ID);
        // 设置用户上下文
        CustomContextUtil.setUserContext(userId);
        PipelineScheduleDTO pipelineScheduleDTO = pipelineScheduleService.queryByToken(scheduleToken);
        if (pipelineScheduleDTO == null) {
            LOGGER.error("[pipelineScheduleTrigger] pipeline schedule not found for token：{}", scheduleToken);
        } else {
            pipelineService.execute(pipelineId,
                    PipelineTriggerTypeEnum.SCHEDULE,
                    null);
        }

    }
}
