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
     * 升级0.18.0-0.19.0，迁移数据
     */
    @JobTask(maxRetryCount = 3, code = "upgradeVersionTo20", description = "升级0.19.0-0.20.0，迁移数据")
    @TimedTask(name = "upgradeVersionTo20", description = "升级0.19.0-0.20.0，迁移数据", oneExecution = true,
            repeatCount = 0, repeatInterval = 1, repeatIntervalUnit = QuartzDefinition.SimpleRepeatIntervalUnit.HOURS, params = {})
    public void syncEnvAppRelevance(Map<String, Object> map) {
        logger.info("begin to upgrade 0.19.0 to 0.20.0 without Cluster and Certification migration.");
        devopsCheckLogService.checkLog("0.20.0");
    }

    /**
     * 升级0.20.0-0.21.0，修复组织管理员的数据
     */
    @JobTask(maxRetryCount = 3, code = "upgradeVersionTo21", description = "升级0.20.0-0.21.0，迁移数据")
    @TimedTask(name = "upgradeVersionTo21", description = "升级0.20.0-0.21.0，迁移数据", oneExecution = true,
            repeatCount = 0, repeatInterval = 1, repeatIntervalUnit = QuartzDefinition.SimpleRepeatIntervalUnit.HOURS, params = {})
    public void syncOrgRootToGitlib(Map<String, Object> map) {
        logger.info("begin to upgrade 0.20.0 to 0.21 without Cluster and Certification migration.");
        devopsCheckLogService.checkLog("0.21.0");
    }

    /**
     * 升级0.20.0-0.21.0，修复组织管理员的数据
     */
    @JobTask(maxRetryCount = 3, code = "upgradeVersionTo21.1", description = "升级0.21.0-0.21.1，迁移数据")
    @TimedTask(name = "upgradeVersionTo21.1", description = "升级0.21.0-0.21.1，迁移数据", oneExecution = true,
            repeatCount = 0, repeatInterval = 1, repeatIntervalUnit = QuartzDefinition.SimpleRepeatIntervalUnit.HOURS, params = {})
    public void syncPipeline(Map<String, Object> map) {
        logger.info("begin to upgrade 0.21.0 to 0.21.1 Pipeline data.");
        devopsCheckLogService.checkLog("0.21.1");
    }

    /**
     * 0.23.0修复harbor的数据
     */
    @JobTask(maxRetryCount = 3, code = "upgradeVersionTo23.0", description = "升级0.22.0-0.23，迁移数据")
    @TimedTask(name = "upgradeVersionTo23.0", description = "升级0.22.0-0.23，迁移数据", oneExecution = true,
            repeatCount = 0, repeatInterval = 1, repeatIntervalUnit = QuartzDefinition.SimpleRepeatIntervalUnit.HOURS, params = {})
    public void fixAppServiceVersion(Map<String, Object> map) {
        logger.info("begin to fix appservice version harbor data.");
        devopsCheckLogService.checkLog("0.23.0");
    }

    /**
     * 0.23.3修复devops_cd_audit和devops_cd_audit_record的 projectId字段数据
     */
    @JobTask(maxRetryCount = 3, code = "upgradeVersionTo23.3", description = "修复project_id字段")
    @TimedTask(name = "upgradeVersionTo23.3", description = "修复project_id字段", oneExecution = true,
            repeatCount = 0, repeatInterval = 1, repeatIntervalUnit = QuartzDefinition.SimpleRepeatIntervalUnit.HOURS, params = {})
    public void fixProjectId(Map<String, Object> map) {
        logger.info("begin to fix projectId ");
        devopsCheckLogService.checkLog("0.23.3");
    }

    /**
     * 同步ldap用户 没有成功创建gitlab用户
     */
    @JobTask(maxRetryCount = 3, code = "upgradeVersionTo0.24", description = "修复同步ldap用户没有成功创建gitlab用户")
    @TimedTask(name = "upgradeVersionTo24", description = "修复同步ldap用户没有成功创建gitlab用户", oneExecution = true,
            repeatCount = 0, repeatInterval = 1, repeatIntervalUnit = QuartzDefinition.SimpleRepeatIntervalUnit.HOURS, params = {})
    public void fixLdapUserToGitLab(Map<String, Object> map) {
        logger.info("begin to fix projectId ");
        devopsCheckLogService.checkLog("0.24.0");
    }

    /**
     * 0.25.0修复数据，增加应用服务的变量
     */
    @JobTask(maxRetryCount = 3, code = "upgradeVersionTo25", description = "增加应用服务的变量")
    @TimedTask(name = "upgradeVersionTo25.3", description = "增加应用服务的变量", oneExecution = true,
            repeatCount = 0, repeatInterval = 1, repeatIntervalUnit = QuartzDefinition.SimpleRepeatIntervalUnit.HOURS, params = {})
    public void fixGitLabAppService(Map<String, Object> map) {
        logger.info("begin to fix gitlab appService ");
        devopsCheckLogService.checkLog("0.25.0");
    }
}
