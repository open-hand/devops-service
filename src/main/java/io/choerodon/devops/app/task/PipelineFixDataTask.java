package io.choerodon.devops.app.task;

import static io.choerodon.devops.app.service.impl.DevopsCheckLogServiceImpl.FIX_APP_CENTER_DATA;

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
 * Created by wangxiang on 2021/10/29
 */
@Component
public class PipelineFixDataTask {
    private static final Logger logger = LoggerFactory.getLogger(PipelineFixDataTask.class);

    private static final String PIPELINE_CONTENT_FIX = "pipelineContentFix";

    private static final String PIPELINE_SONAR_IMAGE_FIX = "pipelineSonarImageFix";

    @Autowired
    private DevopsCheckLogService devopsCheckLogService;

    @JobTask(maxRetryCount = 3, code = PIPELINE_CONTENT_FIX, description = "Saas组织,试用组织流水线content修复")
    @TimedTask(name = PIPELINE_CONTENT_FIX, description = "Saas组织,试用组织流水线content修复", oneExecution = true,
            repeatCount = 0, repeatInterval = 1, repeatIntervalUnit = QuartzDefinition.SimpleRepeatIntervalUnit.HOURS, params = {})
    public void pipelineContentFix(Map<String, Object> map) {
        //试用组织，Saas组织流水线里面使用到的nexus 替换
        try {
            devopsCheckLogService.checkLog(PIPELINE_CONTENT_FIX);
        } catch (Exception e) {
            logger.error("error.fix.data", e);
        }
    }


    @JobTask(maxRetryCount = 3, code = PIPELINE_SONAR_IMAGE_FIX, description = "修复流水线镜像")
    @TimedTask(name = PIPELINE_SONAR_IMAGE_FIX, description = "修复流水线镜像", oneExecution = true,
            repeatCount = 0, repeatInterval = 1, repeatIntervalUnit = QuartzDefinition.SimpleRepeatIntervalUnit.HOURS, params = {})
    public void pipelineSonarImageFix(Map<String, Object> map) {
        try {
            devopsCheckLogService.checkLog(PIPELINE_SONAR_IMAGE_FIX);
        } catch (Exception e) {
            logger.error("error.fix.data", e);
        }
    }
}
