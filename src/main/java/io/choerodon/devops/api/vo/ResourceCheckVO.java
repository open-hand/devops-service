package io.choerodon.devops.api.vo;

import io.swagger.annotations.ApiModelProperty;
import org.hzero.starter.keyencrypt.core.Encrypt;

/**
 * Created by Sheep on 2019/5/15.
 */
public class ResourceCheckVO {

    @ApiModelProperty("通知方式")
    private String method;
    @ApiModelProperty("用户名")
    private String user;
    @Encrypt
    @ApiModelProperty("通知配置id")
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
