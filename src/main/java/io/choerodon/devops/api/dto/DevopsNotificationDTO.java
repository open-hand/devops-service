package io.choerodon.devops.api.dto;

import java.util.List;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  17:23 2019/5/13
 * Description:
 */
public class DevopsNotificationDTO {
    private Long Id;
    private Long envId;
    private String envName;
    private Long projectId;
    private List<String> notifyTriggerEvent;
    private String notifyObject;
    private List<String> notifyType;
    private List<DevopsNotificationUserRelDTO> userRelDTOS;
    private Long objectVersionNumber;


    public Long getId() {
        return Id;
    }

    public void setId(Long id) {
        Id = id;
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

    public List<String> getNotifyTriggerEvent() {
        return notifyTriggerEvent;
    }

    public void setNotifyTriggerEvent(List<String> notifyTriggerEvent) {
        this.notifyTriggerEvent = notifyTriggerEvent;
    }

    public String getNotifyObject() {
        return notifyObject;
    }

    public void setNotifyObject(String notifyObject) {
        this.notifyObject = notifyObject;
    }

    public List<String> getNotifyType() {
        return notifyType;
    }

    public void setNotifyType(List<String> notifyType) {
        this.notifyType = notifyType;
    }

    public List<DevopsNotificationUserRelDTO> getUserRelDTOS() {
        return userRelDTOS;
    }

    public void setUserRelDTOS(List<DevopsNotificationUserRelDTO> userRelDTOS) {
        this.userRelDTOS = userRelDTOS;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public Long getObjectVersionNumber() {
        return objectVersionNumber;
    }

    public void setObjectVersionNumber(Long objectVersionNumber) {
        this.objectVersionNumber = objectVersionNumber;
    }
}
