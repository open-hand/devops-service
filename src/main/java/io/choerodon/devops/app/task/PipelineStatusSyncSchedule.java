package io.choerodon.devops.app.task;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import io.choerodon.devops.app.service.DevopsCiPipelineRecordService;
import io.choerodon.devops.infra.dto.DevopsCiPipelineRecordDTO;

/**
 * 〈功能简述〉
 * 〈流水线状态同步定时任务〉
 *
 * @author wanghao
 * @since 2020/7/14 16:14
 */
@Component
@EnableScheduling
public class PipelineStatusSyncSchedule {
    private static final Logger LOGGER = LoggerFactory.getLogger(PipelineStatusSyncSchedule.class);

    // 多久没有更新状态的则进行同步操作 1000 * 60 * 10  默认10分钟
    @Value("${devops.ci.pipeline.sync.statusUpdatePeriodMilliSeconds:600000}")
    private Long statusUpdatePeriodMilliSeconds;

    @Autowired
    private DevopsCiPipelineRecordService devopsCiPipelineRecordService;

    // 10分钟执行一次数据修复
    @Scheduled(fixedRate = 1000 * 60 * 10)
    public void syncPipelineStatus() {
        LOGGER.info(">>>>>>>>>>>>>>>>>>>>>>>>>>> Start sync pipeline status <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
        // 查询要修复的流水线记录
        List<DevopsCiPipelineRecordDTO> devopsCiPipelineRecordDTOS = devopsCiPipelineRecordService.queryNotSynchronizedRecord(statusUpdatePeriodMilliSeconds);
        devopsCiPipelineRecordDTOS.forEach(v -> {
            devopsCiPipelineRecordService.asyncPipelineUpdate(v.getId(), v.getGitlabPipelineId().intValue());
        });
        // 执行修复逻辑
        LOGGER.info(">>>>>>>>>>>>>>>>>>>>>>>>>>> End sync pipeline status <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
    }
}
