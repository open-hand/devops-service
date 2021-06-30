package io.choerodon.devops.app.task;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.choerodon.asgard.schedule.QuartzDefinition;
import io.choerodon.asgard.schedule.annotation.JobTask;
import io.choerodon.asgard.schedule.annotation.TimedTask;
import io.choerodon.devops.app.service.DevopsCheckLogService;

/**
 * @author zmf
 */
@Component
public class DevopsTask {
    private static final Logger logger = LoggerFactory.getLogger(DevopsTask.class);

    @Autowired
    private DevopsCheckLogService devopsCheckLogService;


    @JobTask(maxRetryCount = 3, code = "fixEnvAppData", description = "修复环境应用服务数据")
//    @TimedTask(name = "fixEnvAppData", description = "修复环境应用服务数据", oneExecution = true,
//            repeatCount = 0, repeatInterval = 1, repeatIntervalUnit = QuartzDefinition.SimpleRepeatIntervalUnit.HOURS, params = {})
    public void fixEnvAppData(Map<String, Object> map) {
        logger.info(">>>>>>>>>>>>>>>>>>>>begin to fix env app data<<<<<<<<<<<<<<<<<<<<<<<<<<");
        try {
            devopsCheckLogService.checkLog("1.1.0");
        } catch (Exception e) {
            logger.error("error.fix.env.app.data", e);
        }
        logger.info(">>>>>>>>>>>>>>>>>>>>end fix env app data<<<<<<<<<<<<<<<<<<<<<<<<<<");
    }
}
