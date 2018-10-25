package io.choerodon.devops.api.dto;

/**
 * Created by n!Ck
 * Date: 2018/10/25
 * Time: 11:32
 * Description:
 */
public class EnvUserPermissionDTO {
    private String loginName;
    private String userName;
    private Boolean permission;

    public EnvUserPermissionDTO() {
    }

    public EnvUserPermissionDTO(String loginName, String userName, Boolean permission) {
        this.loginName = loginName;
        this.userName = userName;
        this.permission = permission;
    }

    public String getUserName() {
        return userName;
    }

    public String getLoginName() {
        return loginName;
    }

    public void setLoginName(String loginName) {
        this.loginName = loginName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public Boolean getDeployment() {
        return permission;
    }

    public void setDeployment(Boolean deployment) {
        permission = deployment;
    }
}
