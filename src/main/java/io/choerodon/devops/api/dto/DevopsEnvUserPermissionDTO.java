package io.choerodon.devops.api.dto;

/**
 * Created by n!Ck
 * Date: 2018/10/25
 * Time: 11:32
 * Description:
 */
public class DevopsEnvUserPermissionDTO {
    private String loginName;
    private String realName;
    private Boolean permission;

    public DevopsEnvUserPermissionDTO() {
    }

    public DevopsEnvUserPermissionDTO(String loginName, String realName, Boolean permission) {
        this.loginName = loginName;
        this.realName = realName;
        this.permission = permission;
    }

    public String getRealName() {
        return realName;
    }

    public String getLoginName() {
        return loginName;
    }

    public void setLoginName(String loginName) {
        this.loginName = loginName;
    }

    public void setRealName(String realName) {
        this.realName = realName;
    }

    public Boolean getDeployment() {
        return permission;
    }

    public void setDeployment(Boolean deployment) {
        permission = deployment;
    }
}
