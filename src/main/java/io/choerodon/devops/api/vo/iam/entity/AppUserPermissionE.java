package io.choerodon.devops.api.vo.iam.entity;

/**
 * Created by n!Ck
 * Date: 2018/11/21
 * Time: 11:16
 * Description:
 */
public class AppUserPermissionE {
    private Long iamUserId;
    private Long appId;

    public AppUserPermissionE() {
    }

    public AppUserPermissionE(Long iamUserId, Long appId) {
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
