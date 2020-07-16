package io.choerodon.devops.api.vo;

import java.util.List;

import org.hzero.starter.keyencrypt.core.Encrypt;

/**
 * User: Mr.Wang
 * Date: 2019/12/12
 */
public class DevopsNotificationTransferDataVO {
    @Encrypt
    private Long id;
    @Encrypt
    private Long envId;
    private String envName;
    private Long projectId;
    private String notifyTriggerEvent;
    private String notifyObject;
    private String notifyType;
    private List<DevopsNotificationUserRelVO> userRelDTOS;
    private Long objectVersionNumber;
    @Encrypt
    private List<Long> userRelIds;

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

    public String getEnvName() {
        return envName;
    }

    public void setEnvName(String envName) {
        this.envName = envName;
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

    public List<DevopsNotificationUserRelVO> getUserRelDTOS() {
        return userRelDTOS;
    }

    public void setUserRelDTOS(List<DevopsNotificationUserRelVO> userRelDTOS) {
        this.userRelDTOS = userRelDTOS;
    }

    public Long getObjectVersionNumber() {
        return objectVersionNumber;
    }

    public void setObjectVersionNumber(Long objectVersionNumber) {
        this.objectVersionNumber = objectVersionNumber;
    }

    public List<Long> getUserRelIds() {
        return userRelIds;
    }

    public void setUserRelIds(List<Long> userRelIds) {
        this.userRelIds = userRelIds;
    }
}
