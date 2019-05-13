package io.choerodon.devops.api.dto;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  17:27 2019/5/13
 * Description:
 */
public class DevopsNotificationUserRelDTO {
    private Long Id;
    private Long userId;
    private Long notificationId;
    private String imageUrl;

    public Long getUserId() {
        return userId;
    }

    public Long getId() {
        return Id;
    }

    public void setId(Long id) {
        Id = id;
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

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
