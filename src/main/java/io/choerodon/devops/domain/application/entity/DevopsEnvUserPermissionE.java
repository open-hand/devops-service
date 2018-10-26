package io.choerodon.devops.domain.application.entity;

/**
 * Created by n!Ck
 * Date: 2018/10/26
 * Time: 11:11
 * Description:
 */

public class DevopsEnvUserPermissionE {
    private String loginName;
    private String realName;
    private Long envId;
    private Boolean permission;

    public DevopsEnvUserPermissionE() {
    }

    public DevopsEnvUserPermissionE(String loginName, String realName, Long envId, Boolean permission) {
        this.loginName = loginName;
        this.realName = realName;
        this.envId = envId;
        this.permission = permission;
    }

    public String getLoginName() {
        return loginName;
    }

    public void setLoginName(String loginName) {
        this.loginName = loginName;
    }

    public String getRealName() {
        return realName;
    }

    public void setRealName(String realName) {
        this.realName = realName;
    }

    public Long getEnvId() {
        return envId;
    }

    public void setEnvId(Long envId) {
        this.envId = envId;
    }

    public Boolean getPermission() {
        return permission;
    }

    public void setPermission(Boolean permission) {
        this.permission = permission;
    }
}
