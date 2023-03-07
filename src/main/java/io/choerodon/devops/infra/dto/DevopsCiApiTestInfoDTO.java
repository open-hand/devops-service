package io.choerodon.devops.infra.dto;

import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import io.choerodon.mybatis.annotation.ModifyAudit;
import io.choerodon.mybatis.annotation.VersionAudit;
import io.choerodon.mybatis.domain.AuditDomain;

/**
 * devops_cd_api_test_info(DevopsCdApiTestInfo)实体类
 *
 * @author hao.li@zknow.com
 * @since 2022-11-07
 */

@ApiModel("devops_ci_api_test_info")
@VersionAudit
@ModifyAudit
@JsonInclude(value = JsonInclude.Include.NON_NULL)
@Table(name = "devops_ci_api_test_info")
public class DevopsCiApiTestInfoDTO extends AuditDomain {
    public static final String FIELD_ID = "id";
    public static final String FIELD_API_TEST_TASK_ID = "apiTestTaskId";
    public static final String FIELD_API_TEST_SUITE_ID = "apiTestSuiteId";
    public static final String FIELD_API_TEST_CONFIG_ID = "apiTestConfigId";
    public static final String FIELD_DEPLOY_JOB_NAME = "deployJobName";
    public static final String FIELD_ENABLE_WARNING_SETTING = "enableWarningSetting";
    public static final String FIELD_PERFORM_THRESHOLD = "performThreshold";
    public static final String FIELD_NOTIFY_USER_IDS = "notifyUserIds";
    public static final String FIELD_SEND_EMAIL = "sendEmail";
    public static final String FIELD_SEND_SITE_MESSAGE = "sendSiteMessage";
    public static final String FIELD_BLOCK_AFTER_JOB = "blockAfterJob";
    private static final long serialVersionUID = -88695884351721551L;
    @Id
    @GeneratedValue
    private Long id;
    @ApiModelProperty(value = "关联devops流水线id")
    private Long ciPipelineId;

    /**
     * {@link io.choerodon.devops.infra.enums.test.ApiTestTaskType}
     */
    @ApiModelProperty(value = "测试任务类型")
    private String taskType;

    @ApiModelProperty(value = "测试任务id")
    private Long apiTestTaskId;

    @ApiModelProperty(value = "测试套件id")
    private Long apiTestSuiteId;

    @ApiModelProperty(value = "测试任务关联的任务配置id")
    private Long apiTestConfigId;

    @ApiModelProperty(value = "是否启用告警设置")
    private Boolean enableWarningSetting;

    @ApiModelProperty(value = "阈值")
    private Double performThreshold;

    @ApiModelProperty(value = "通知对象集合")
    private String notifyUserIds;

    @ApiModelProperty(value = "是否发送邮件")
    private Boolean sendEmail;

    @ApiModelProperty(value = "是否发送站内信")
    private Boolean sendSiteMessage;

    @ApiModelProperty(value = "是否中断后续的任务")
    private Boolean blockAfterJob;

    @Transient
    public Long projectId;

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public Long getCiPipelineId() {
        return ciPipelineId;
    }

    public void setCiPipelineId(Long ciPipelineId) {
        this.ciPipelineId = ciPipelineId;
    }

    public String getTaskType() {
        return taskType;
    }

    public void setTaskType(String taskType) {
        this.taskType = taskType;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getApiTestTaskId() {
        return apiTestTaskId;
    }

    public void setApiTestTaskId(Long apiTestTaskId) {
        this.apiTestTaskId = apiTestTaskId;
    }

    public Long getApiTestSuiteId() {
        return apiTestSuiteId;
    }

    public void setApiTestSuiteId(Long apiTestSuiteId) {
        this.apiTestSuiteId = apiTestSuiteId;
    }

    public Long getApiTestConfigId() {
        return apiTestConfigId;
    }

    public void setApiTestConfigId(Long apiTestConfigId) {
        this.apiTestConfigId = apiTestConfigId;
    }

    public Boolean getEnableWarningSetting() {
        return enableWarningSetting;
    }

    public void setEnableWarningSetting(Boolean enableWarningSetting) {
        this.enableWarningSetting = enableWarningSetting;
    }

    public Double getPerformThreshold() {
        return performThreshold;
    }

    public void setPerformThreshold(Double performThreshold) {
        this.performThreshold = performThreshold;
    }

    public String getNotifyUserIds() {
        return notifyUserIds;
    }

    public void setNotifyUserIds(String notifyUserIds) {
        this.notifyUserIds = notifyUserIds;
    }

    public Boolean getSendEmail() {
        return sendEmail;
    }

    public void setSendEmail(Boolean sendEmail) {
        this.sendEmail = sendEmail;
    }

    public Boolean getSendSiteMessage() {
        return sendSiteMessage;
    }

    public void setSendSiteMessage(Boolean sendSiteMessage) {
        this.sendSiteMessage = sendSiteMessage;
    }

    public Boolean getBlockAfterJob() {
        return blockAfterJob;
    }

    public void setBlockAfterJob(Boolean blockAfterJob) {
        this.blockAfterJob = blockAfterJob;
    }

}

