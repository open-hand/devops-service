package io.choerodon.devops.api.vo;

/**
 * Created by n!Ck
 * Date: 2018/10/25
 * Time: 11:32
 * Description:
 */
public class DevopsEnvUserPermissionVO {
    private Long iamUserId;
    private String loginName;
    private String realName;
    private Boolean projectOwner;

    public Boolean getProjectOwner() {
        return projectOwner;
    }

    public void setProjectOwner(Boolean projectOwner) {
        this.projectOwner = projectOwner;
    }

    public DevopsEnvUserPermissionVO() {
    }

    public DevopsEnvUserPermissionVO(String loginName, Long iamUserId, String realName) {
        this.loginName = loginName;
        this.iamUserId = iamUserId;
        this.realName = realName;
    }

    public Long getIamUserId() {
        return iamUserId;
    }

    public void setIamUserId(Long iamUserId) {
        this.iamUserId = iamUserId;
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
}
