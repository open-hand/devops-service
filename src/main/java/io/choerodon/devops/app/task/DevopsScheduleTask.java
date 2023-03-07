package io.choerodon.devops.app.task;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import io.choerodon.devops.app.service.AppExceptionRecordService;
import io.choerodon.devops.app.service.DevopsCiPipelineRecordService;
import io.choerodon.devops.app.service.DevopsClusterNodeService;
import io.choerodon.devops.infra.dto.DevopsCiPipelineRecordDTO;

/**
 * 〈功能简述〉
 * 〈devops中的一些定时任务类〉
 *
 * @author wanghao
 * @since 2020/7/14 16:14
 */
@ConditionalOnProperty(value = "local.test", havingValue = "false", matchIfMissing = true)
@Component
@EnableScheduling
public class DevopsScheduleTask {
    private static final Logger LOGGER = LoggerFactory.getLogger(DevopsScheduleTask.class);

    // 多久没有更新状态的则进行同步操作 1000 * 60 * 10  默认10分钟
    @Value("${devops.ci.pipeline.sync.statusUpdatePeriodMilliSeconds:600000}")
    private Long statusUpdatePeriodMilliSeconds;
    // 应用监控异常数据保留时长，默认180天
    @Value("${devops.clearExceptionRecordPeriod:180}")
    private Long clearExceptionRecordPeriod;

    @Autowired
    private DevopsCiPipelineRecordService devopsCiPipelineRecordService;
    @Autowired
    private AppExceptionRecordService appExceptionRecordService;
    @Autowired
    private DevopsClusterNodeService devopsClusterNodeService;

    /**
     * 流水线记录状态同步定时任务，10分钟执行一次数据修复
     */
    @Scheduled(fixedRate = 1000 * 60 * 10)
    public void syncPipelineStatus() {
        LOGGER.info(">>>>>>>>>>>>>>>>>>>>>>>>>>> Start sync pipeline status <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
        // 查询要修复的流水线记录
        List<DevopsCiPipelineRecordDTO> devopsCiPipelineRecordDTOS = devopsCiPipelineRecordService.queryNotSynchronizedRecord(statusUpdatePeriodMilliSeconds);
        devopsCiPipelineRecordDTOS.forEach(v -> devopsCiPipelineRecordService.asyncPipelineUpdate(v.getId(), v.getGitlabPipelineId().intValue()));
        // 执行修复逻辑
        LOGGER.info(">>>>>>>>>>>>>>>>>>>>>>>>>>> End sync pipeline status <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
    }

    /**
     * 定时清理应用监控数据，
     * 保留时长：默认保留180天
     * 执行频率：每天02 : 00
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void clearExceptionRecords() {
        LOGGER.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> Start clear app exception records <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
        LocalDate now = LocalDate.now();
        LocalDate localDate = now.plusDays(-clearExceptionRecordPeriod);
        appExceptionRecordService.clearRecordsBeforeDate(Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant()));

        LOGGER.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> Finish clear app exception records <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
    }

    /**
     * 定时更新集群安装状态
     */
    @Scheduled(cron = "0 0/3 * * * ?")
    public void updateCluster() {
        devopsClusterNodeService.update();
    }
}
