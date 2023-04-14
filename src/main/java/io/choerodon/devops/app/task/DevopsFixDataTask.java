package io.choerodon.devops.app.task;

import static io.choerodon.devops.app.service.impl.DevopsCheckLogServiceImpl.*;

import java.util.Map;

import com.yqcloud.core.oauth.ZKnowDetailsHelper;
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


    /**
     * 删除devops_env_resource_detail脏数据
     *
     * @param map
     */
    @JobTask(productSource = ZKnowDetailsHelper.VALUE_CHOERODON, maxRetryCount = 3, code = DELETE_DEVOPS_ENV_RESOURCE_DETAIL_DATA, description = "删除资源详情脏数据")
//    @TimedTask(name = DELETE_DEVOPS_ENV_RESOURCE_DETAIL_DATA,
//            description = "删除资源详情脏数据",
//            params = {},
//            triggerType = TriggerTypeEnum.CRON_TRIGGER,
//            cronExpression = "0 0 1 * * ?")
    public void deleteDevopsEnvResourceDetailData(Map<String, Object> map) {
        try {
            devopsCheckLogService.checkLog(DELETE_DEVOPS_ENV_RESOURCE_DETAIL_DATA);
        } catch (Exception e) {
            logger.error("devops.fix.data", e);
        }
    }

    @JobTask(productSource = ZKnowDetailsHelper.VALUE_CHOERODON, maxRetryCount = 3, code = FIX_PIPELINE_SONAR_DATA, description = "修复流水线代码检查脚本")
    @TimedTask(name = FIX_PIPELINE_SONAR_DATA,
            description = "修复流水线代码检查脚本",
            repeatInterval = 1,
            repeatIntervalUnit = QuartzDefinition.SimpleRepeatIntervalUnit.HOURS,
            params = {})
    public void fixPipelineSonarData(Map<String, Object> map) {
        try {
            devopsCheckLogService.checkLog(FIX_PIPELINE_SONAR_DATA);
        } catch (Exception e) {
            logger.error("devops.fix.data", e);
        }
    }


    @JobTask(productSource = ZKnowDetailsHelper.VALUE_CHOERODON, maxRetryCount = 3, code = FIX_CERTIFICATE_TYPE, description = "修复证书类型")
    @TimedTask(name = FIX_CERTIFICATE_TYPE,
            description = "修复证书类型",
            repeatInterval = 1,
            repeatIntervalUnit = QuartzDefinition.SimpleRepeatIntervalUnit.HOURS,
            params = {})
    public void fixCertificateType(Map<String, Object> map) {
        try {
            devopsCheckLogService.checkLog(FIX_CERTIFICATE_TYPE);
        } catch (Exception e) {
            logger.error("devops.fix.data", e);
        }
    }

}
