package io.choerodon.devops.api.vo;

/**
 * Created by Sheep on 2019/5/15.
 */
public class ResourceCheckDTO {

    private String method;

    private String user;

    private Long notificationId;

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public Long getNotificationId() {
        return notificationId;
    }

    public void setNotificationId(Long notificationId) {
        this.notificationId = notificationId;
    }
}
