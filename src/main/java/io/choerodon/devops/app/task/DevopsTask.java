package io.choerodon.devops.app.task;

import java.util.*;

import io.choerodon.asgard.schedule.QuartzDefinition;
import io.choerodon.asgard.schedule.annotation.JobParam;
import io.choerodon.asgard.schedule.annotation.JobTask;
import io.choerodon.asgard.schedule.annotation.TaskParam;
import io.choerodon.asgard.schedule.annotation.TimedTask;
import io.choerodon.devops.app.service.DevopsCheckLogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author zmf
 */
@Component
public class DevopsTask {
    private static final Logger logger = LoggerFactory.getLogger(DevopsTask.class);

    @Autowired
    private DevopsCheckLogService devopsCheckLogService;

    @JobTask(maxRetryCount = 3, code = "syncDeployValue", params = {
            @JobParam(name = "test", defaultValue = "test")
    }, description = "升级到0.18.0,修复部署values")
    @TimedTask(name = "syncDeployValue", description = "升级到0.18.0,修复部署values", oneExecution = true,
            repeatCount = 0, repeatInterval = 1, repeatIntervalUnit = QuartzDefinition.SimpleRepeatIntervalUnit.HOURS, params = {
            @TaskParam(name = "test", value = "test")
    })
    public void syncDeployValue(Map<String, Object> map) {
        logger.info("begin to upgrade 0.17.0 to 0.18.0");
        devopsCheckLogService.checkLog("0.18.0");
    }

    /**
     * 修复环境下关联应用数据
     */
    @JobTask(maxRetryCount = 3, code = "syncEnvAppRelevance", params = {
            @JobParam(name = "fixenvapp", defaultValue = "fixenvapp")
    }, description = "修复环境下关联应用数据")
    @TimedTask(name = "syncDeployRelevance", description = "修复环境下关联应用数据", oneExecution = true,
            repeatCount = 0, repeatInterval = 1, repeatIntervalUnit = QuartzDefinition.SimpleRepeatIntervalUnit.HOURS, params = {
            @TaskParam(name = "fixenvapp", value = "fixenvapp")
    })
    public void syncEnvAppRelevance(Map<String, Object> map) {
        logger.info("begin to upgrade 0.18.0 to 0.19.0");
        devopsCheckLogService.checkLog("0.19.0");
    }


}
