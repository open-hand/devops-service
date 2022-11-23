package io.choerodon.devops.app.task;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import io.choerodon.devops.app.service.CdTaskSchedulerService;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2022/11/23 17:22
 */
@Component
public class CdTaskScheduler {

    @Autowired
    private CdTaskSchedulerService cdTaskSchedulerService;


    /**
     * 每5s执行一次
     */
    @Scheduled(cron = "0/5 * * * * ?")
    public void addTasks() {
        cdTaskSchedulerService.schedulePeriodically();
    }
}
