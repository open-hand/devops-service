package io.choerodon.devops.app.task;

import static io.choerodon.devops.app.service.impl.DevopsCheckLogServiceImpl.FIX_APP_CENTER_DATA;
import static io.choerodon.devops.app.service.impl.DevopsCheckLogServiceImpl.FIX_ENV_DATA;

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
public class DevopsFixDataTask {
    private static final Logger logger = LoggerFactory.getLogger(DevopsFixDataTask.class);

    @Autowired
    private DevopsCheckLogService devopsCheckLogService;



    @JobTask(maxRetryCount = 3, code = FIX_APP_CENTER_DATA, description = "修复应用中心数据")
    @TimedTask(name = FIX_APP_CENTER_DATA, description = "修复应用中心数据", oneExecution = true,
            repeatCount = 0, repeatInterval = 1, repeatIntervalUnit = QuartzDefinition.SimpleRepeatIntervalUnit.HOURS, params = {})
    public void fixAppCenterData(Map<String, Object> map) {
        try {
            devopsCheckLogService.checkLog(FIX_APP_CENTER_DATA);
        } catch (Exception e) {
            logger.error("error.fix.data", e);
        }
    }
}