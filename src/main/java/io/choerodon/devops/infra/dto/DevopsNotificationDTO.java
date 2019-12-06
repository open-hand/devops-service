package io.choerodon.devops.infra.dto;

import io.choerodon.mybatis.entity.BaseDTO;
import io.swagger.annotations.ApiModelProperty;

import javax.persistence.*;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  17:37 2019/5/13
 * Description:
 */
@Table(name = "devops_notification")
public class DevopsNotificationDTO extends BaseDTO {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long envId;
    private Long projectId;
    private String notifyTriggerEvent;
    private String notifyObject;
    private String notifyType;

    @ApiModelProperty(value = "是否发送邮件")
    @Column(name = "is_send_email")
    private Boolean sendEmail;

    @ApiModelProperty(value = "是否发送短信")
    @Column(name = "is_send_sms")
    private Boolean sendSms;

    @ApiModelProperty(value = "是否发送站内信")
    @Column(name = "is_send_pm")
    private Boolean sendPm;

    @ApiModelProperty(value = "是否是默认设置")
    @Column(name = "is_default_setting")
    private Boolean defaultSetting;

    @Transient
    private String envName;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getEnvId() {
        return envId;
    }

    public void setEnvId(Long envId) {
        this.envId = envId;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public String getEnvName() {
        return envName;
    }

    public void setEnvName(String envName) {
        this.envName = envName;
    }

    public String getNotifyTriggerEvent() {
        return notifyTriggerEvent;
    }

    public void setNotifyTriggerEvent(String notifyTriggerEvent) {
        this.notifyTriggerEvent = notifyTriggerEvent;
    }

    public String getNotifyObject() {
        return notifyObject;
    }

    public void setNotifyObject(String notifyObject) {
        this.notifyObject = notifyObject;
    }

    public String getNotifyType() {
        return notifyType;
    }

    public void setNotifyType(String notifyType) {
        this.notifyType = notifyType;
    }

    public Boolean getSendEmail() {
        return sendEmail;
    }

    public void setSendEmail(Boolean sendEmail) {
        this.sendEmail = sendEmail;
    }

    public Boolean getSendSms() {
        return sendSms;
    }

    public void setSendSms(Boolean sendSms) {
        this.sendSms = sendSms;
    }

    public Boolean getSendPm() {
        return sendPm;
    }

    public void setSendPm(Boolean sendPm) {
        this.sendPm = sendPm;
    }

    public Boolean getDefaultSetting() {
        return defaultSetting;
    }

    public void setDefaultSetting(Boolean defaultSetting) {
        this.defaultSetting = defaultSetting;
    }
}
