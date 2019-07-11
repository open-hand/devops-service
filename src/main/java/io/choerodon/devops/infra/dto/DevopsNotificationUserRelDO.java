package io.choerodon.devops.infra.dto;

import javax.persistence.Table;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  17:39 2019/5/13
 * Description:
 */
@Table(name = "devops_notification_user_rel")
public class DevopsNotificationUserRelDO {

    private Long userId;
    private Long notificationId;

    public DevopsNotificationUserRelDO() {
    }

    public DevopsNotificationUserRelDO(Long userId, Long notificationId) {
        this.userId = userId;
        this.notificationId = notificationId;
    }

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
}
