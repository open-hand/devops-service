package io.choerodon.devops.domain.application.task;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.choerodon.asgard.schedule.QuartzDefinition;
import io.choerodon.asgard.schedule.annotation.JobParam;
import io.choerodon.asgard.schedule.annotation.JobTask;
import io.choerodon.asgard.schedule.annotation.TaskParam;
import io.choerodon.asgard.schedule.annotation.TimedTask;
import io.choerodon.devops.app.service.DevopsCheckLogService;

@Component
public class DevopsTask {


    private static final Logger logger = LoggerFactory.getLogger(DevopsTask.class);

    @Autowired
    private DevopsCheckLogService devopsCheckLogService;

    @JobTask(maxRetryCount = 1, code = "syncGitlabUserName", params = {
            @JobParam(name = "test", defaultValue = "test")
    }, description = "升级到0.12.0同步gitlab用户名")
    @TimedTask(name = "升级到0.12.0同步gitlab用户名", description = "自定义定时任务", oneExecution = true,
            repeatCount = 0, repeatInterval = 1000, repeatIntervalUnit = QuartzDefinition.SimpleRepeatIntervalUnit.HOURS, params = {
            @TaskParam(name = "test", value = "test")
    })
    public void syncGitlabUserName(Map<String, Object> map) {
        logger.info("begin to sync gitlab userName!");
        devopsCheckLogService.checkLog("0.12.0");
    }


}
