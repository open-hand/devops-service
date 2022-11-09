package io.choerodon.devops.api.vo.pipeline;

import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import io.swagger.annotations.ApiModelProperty;
import org.hzero.starter.keyencrypt.core.Encrypt;

public class DevopsCiApiTestInfoVO {
    @Id
    @GeneratedValue
    @Encrypt
    private Long id;

    /**
     * {@link io.choerodon.devops.infra.enums.test.ApiTestTaskType}
     */
    @ApiModelProperty(value = "测试任务类型")
    private String taskType;

    @ApiModelProperty(value = "测试任务id")
    @Encrypt
    private Long apiTestTaskId;

    @ApiModelProperty(value = "测试套件id")
    @Encrypt
    private Long apiTestSuiteId;

    @ApiModelProperty(value = "测试任务关联的任务配置id")
    @Encrypt
    private Long apiTestConfigId;

    @ApiModelProperty(value = "部署任务名称")
    private String deployJobName;

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

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTaskType() {
        return taskType;
    }

    public void setTaskType(String taskType) {
        this.taskType = taskType;
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

    public String getDeployJobName() {
        return deployJobName;
    }

    public void setDeployJobName(String deployJobName) {
        this.deployJobName = deployJobName;
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
