package io.choerodon.devops.api.vo;

import io.swagger.annotations.ApiModelProperty;

import java.util.List;

/**
 * 〈功能简述〉
 * 〈删除验证通知事件VO〉
 *
 * @author wanghao
 * @Date 2019/12/5 15:08
 */
public class NotificationEventVO {

    private Long id;
    private Long envId;
    private Long projectId;

    @ApiModelProperty(value = "通知事件类型")
    private String notifyTriggerEvent;

    @ApiModelProperty(value = "是否发送邮件")
    private Boolean sendEmail;

    @ApiModelProperty(value = "是否发送短信")
    private Boolean sendSms;

    @ApiModelProperty(value = "是否发送站内信")
    private Boolean sendPm;

    @ApiModelProperty(name = "是否发送给操作者")
    private Boolean sendHandler = false;

    @ApiModelProperty(name = "是否发送给所有者")
    private Boolean sendOwner = false;

    @ApiModelProperty(name = "是否发送指定用户")
    private Boolean sendSpecifier = false;

    private List<DevopsNotificationUserRelVO> userList;

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

    public String getNotifyTriggerEvent() {
        return notifyTriggerEvent;
    }

    public void setNotifyTriggerEvent(String notifyTriggerEvent) {
        this.notifyTriggerEvent = notifyTriggerEvent;
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

    public List<DevopsNotificationUserRelVO> getUserList() {
        return userList;
    }

    public void setUserList(List<DevopsNotificationUserRelVO> userList) {
        this.userList = userList;
    }

    public Boolean getSendHandler() {
        return sendHandler;
    }

    public void setSendHandler(Boolean sendHandler) {
        this.sendHandler = sendHandler;
    }

    public Boolean getSendOwner() {
        return sendOwner;
    }

    public void setSendOwner(Boolean sendOwner) {
        this.sendOwner = sendOwner;
    }

    public Boolean getSendSpecifier() {
        return sendSpecifier;
    }

    public void setSendSpecifier(Boolean sendSpecifier) {
        this.sendSpecifier = sendSpecifier;
    }
}
