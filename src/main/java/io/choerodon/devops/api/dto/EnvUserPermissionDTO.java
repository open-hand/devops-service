package io.choerodon.devops.api.dto;

/**
 * Created by n!Ck
 * Date: 2018/10/25
 * Time: 11:32
 * Description:
 */
public class EnvUserPermissionDTO {
    private Long id;
    private String userName;
    private String loginName;
    private Boolean isDeployment;

    public EnvUserPermissionDTO() {
    }

    public EnvUserPermissionDTO(Long id, String userName, String loginName, Boolean isDeployment) {
        this.id = id;
        this.userName = userName;
        this.loginName = loginName;
        this.isDeployment = isDeployment;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getLoginName() {
        return loginName;
    }

    public void setLoginName(String loginName) {
        this.loginName = loginName;
    }

    public Boolean getDeployment() {
        return isDeployment;
    }

    public void setDeployment(Boolean deployment) {
        isDeployment = deployment;
    }
}
