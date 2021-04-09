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

    /**
     * 0.25.0修复数据，增加应用服务的变量
     */
    @JobTask(maxRetryCount = 3, code = "devopsUpgradeVersionTo25", description = "增加应用服务的变量")
    @TimedTask(name = "devopsUpgradeVersionTo25", description = "增加应用服务的变量", oneExecution = true,
            repeatCount = 0, repeatInterval = 1, repeatIntervalUnit = QuartzDefinition.SimpleRepeatIntervalUnit.HOURS, params = {})
    public void fixGitLabAppService(Map<String, Object> map) {
        logger.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>begin to fix gitlab appService ");
        devopsCheckLogService.checkLog("0.25.0");
    }
}
