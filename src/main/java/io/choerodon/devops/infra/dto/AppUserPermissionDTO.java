package io.choerodon.devops.infra.dto;

import javax.persistence.Table;

/**
 * Created by n!Ck
 * Date: 2018/11/21
 * Time: 11:08
 * Description:
 */

@Table(name = "devops_app_user_rel")
public class AppUserPermissionDTO {
    private Long iamUserId;
    private Long appId;

    public AppUserPermissionDTO() {
    }

    public AppUserPermissionDTO(Long iamUserId, Long appId) {
        this.iamUserId = iamUserId;
        this.appId = appId;
    }

    public Long getIamUserId() {
        return iamUserId;
    }

    public void setIamUserId(Long iamUserId) {
        this.iamUserId = iamUserId;
    }

    public Long getAppId() {
        return appId;
    }

    public void setAppId(Long appId) {
        this.appId = appId;
    }
}
