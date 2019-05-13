package io.choerodon.devops.domain.application.entity;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  17:36 2019/5/13
 * Description:
 */
public class DevopsNotificationUserRelE {
    private Long Id;
    private Long userId;
    private Long notificationId;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getNotificationId() {
        return notificationId;
    }

    public void setNotificationId(Long notificationId) {
        this.notificationId = notificationId;
    }

    public Long getId() {
        return Id;
    }

    public void setId(Long id) {
        Id = id;
    }
}
