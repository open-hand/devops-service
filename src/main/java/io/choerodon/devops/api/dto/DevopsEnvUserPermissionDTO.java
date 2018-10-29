package io.choerodon.devops.api.dto;

/**
 * Created by n!Ck
 * Date: 2018/10/25
 * Time: 11:32
 * Description:
 */
public class DevopsEnvUserPermissionDTO {
    private String loginName;
    private Long iamUserId;
    private String realName;
    private Boolean isPermitted;

    public DevopsEnvUserPermissionDTO() {
    }

    public DevopsEnvUserPermissionDTO(String loginName, Long iamUserId, String realName, Boolean isPermitted) {
        this.loginName = loginName;
        this.iamUserId = iamUserId;
        this.realName = realName;
        this.isPermitted = isPermitted;
    }

    public String getRealName() {
        return realName;
    }

    public String getLoginName() {
        return loginName;
    }

    public Long getIamUserId() {
        return iamUserId;
    }

    public void setIamUserId(Long iamUserId) {
        this.iamUserId = iamUserId;
    }

    public void setLoginName(String loginName) {
        this.loginName = loginName;
    }

    public void setRealName(String realName) {
        this.realName = realName;
    }

    public Boolean getPermitted() {
        return isPermitted;
    }

    public void setPermitted(Boolean permitted) {
        isPermitted = permitted;
    }
}
