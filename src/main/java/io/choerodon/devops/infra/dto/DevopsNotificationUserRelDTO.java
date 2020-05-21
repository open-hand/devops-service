package io.choerodon.devops.infra.dto;

import javax.persistence.Table;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  17:39 2019/5/13
 * Description:
 */
@Table(name = "devops_notification_user_rel")
public class DevopsNotificationUserRelDTO {
<<<<<<< HEAD
=======
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
>>>>>>> [UPD] update strategy of @GeneratedValue to GenerationType.AUTO

    private Long userId;
    private Long notificationId;

    public DevopsNotificationUserRelDTO() {
    }

    public DevopsNotificationUserRelDTO(Long userId, Long notificationId) {
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
