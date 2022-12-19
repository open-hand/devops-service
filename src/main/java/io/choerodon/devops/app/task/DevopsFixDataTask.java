package io.choerodon.devops.app.task;

import static io.choerodon.devops.app.service.impl.DevopsCheckLogServiceImpl.*;

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
            logger.error("devops.fix.data", e);
        }
    }

    /**
     * v1.2.0 执行此次任务需要保证，1.2.0-alpha版本的数据修复任务执行成功才行。即1.1版本必须先升级1.2.0-alpha版本，在升级1.2版本
     *
     * @param map
     */
    @JobTask(maxRetryCount = 3, code = FIX_PIPELINE_DATA, description = "修复流水线数据")
    @TimedTask(name = FIX_PIPELINE_DATA, description = "修复流水线数据", repeatInterval = 1, repeatIntervalUnit = QuartzDefinition.SimpleRepeatIntervalUnit.HOURS, params = {})
    public void fixPipelineData(Map<String, Object> map) {
        try {
            devopsCheckLogService.checkLog(FIX_PIPELINE_DATA);
        } catch (Exception e) {
            logger.error("devops.fix.data", e);
        }
    }

    /**
     * 迁移helm仓库的数据到新的表，以及应用版本与helm仓库的关联关系
     *
     * @param map
     */
    @JobTask(maxRetryCount = 3, code = FIX_HELM_REPO_DATA, description = "迁移helm仓库数据")
    @TimedTask(name = FIX_HELM_REPO_DATA, description = "迁移helm仓库数据", repeatInterval = 1, repeatIntervalUnit = QuartzDefinition.SimpleRepeatIntervalUnit.HOURS, params = {})
    public void fixHelmRepoData(Map<String, Object> map) {
        try {
            devopsCheckLogService.checkLog(FIX_HELM_REPO_DATA);
        } catch (Exception e) {
            logger.error("devops.fix.data", e);
        }
    }

    @JobTask(maxRetryCount = 3, code = FIX_HELM_VERSION_DATA, description = "迁移应用服务版本中chart版本数据")
    @TimedTask(name = FIX_HELM_VERSION_DATA, description = "迁移应用服务版本中chart版本数据", repeatInterval = 1, repeatIntervalUnit = QuartzDefinition.SimpleRepeatIntervalUnit.HOURS, params = {})
    public void fixHelmVersionData(Map<String, Object> map) {
        try {
            devopsCheckLogService.checkLog(FIX_HELM_VERSION_DATA);
        } catch (Exception e) {
            logger.error("devops.fix.data", e);
        }
    }


    @JobTask(maxRetryCount = 3, code = FIX_HELM_IMAGE_VERSION_OF_NULL_DATA, description = "迁移遗漏的应用版本数据")
    @TimedTask(name = FIX_HELM_IMAGE_VERSION_OF_NULL_DATA, description = "迁移遗漏的应用版本数据", repeatInterval = 1, repeatIntervalUnit = QuartzDefinition.SimpleRepeatIntervalUnit.HOURS, params = {})
    public void fixRemainHelmVersionData(Map<String, Object> map) {
        try {
            devopsCheckLogService.checkLog(FIX_HELM_IMAGE_VERSION_OF_NULL_DATA);
        } catch (Exception e) {
            logger.error("devops.fix.data", e);
        }
    }

    /**
     * 迁移应用服务版本中镜像版本数据
     *
     * @param map
     */
    @JobTask(maxRetryCount = 3, code = FIX_IMAGE_VERSION_DATA, description = "迁移应用服务版本中镜像版本数据")
    @TimedTask(name = FIX_IMAGE_VERSION_DATA, description = "迁移应用服务版本中镜像版本数据", repeatInterval = 1, repeatIntervalUnit = QuartzDefinition.SimpleRepeatIntervalUnit.HOURS, params = {})
    public void fixImageVersionData(Map<String, Object> map) {
        try {
            devopsCheckLogService.checkLog(FIX_IMAGE_VERSION_DATA);
        } catch (Exception e) {
            logger.error("devops.fix.data", e);
        }
    }

    /**
     * 删除devops_env_resource_detail脏数据
     *
     * @param map
     */
    @JobTask(maxRetryCount = 3, code = DELETE_DEVOPS_ENV_RESOURCE_DETAIL_DATA, description = "删除资源详情脏数据")
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

    /**
     * cicd融合迁移cd数据
     *
     * @param map
     */
    @JobTask(maxRetryCount = 3, code = MIGRATION_CD_PIPELINE_DATE, description = "cicd融合迁移cd数据")
    @TimedTask(name = MIGRATION_CD_PIPELINE_DATE,
            description = "cicd融合迁移cd数据",
            repeatInterval = 1,
            repeatIntervalUnit = QuartzDefinition.SimpleRepeatIntervalUnit.HOURS,
            params = {})
    public void migrationCdPipelineDate(Map<String, Object> map) {
        try {
            devopsCheckLogService.checkLog(MIGRATION_CD_PIPELINE_DATE);
        } catch (Exception e) {
            logger.error("devops.fix.data", e);
        }
    }


}
