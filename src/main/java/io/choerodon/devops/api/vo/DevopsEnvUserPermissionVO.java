package io.choerodon.devops.api.vo;

import java.util.Date;

/**
 * @author zmf
 */
public class DevopsEnvUserPermissionVO extends DevopsEnvUserVO {
    private String role;
    private Date creationDate;

    public DevopsEnvUserPermissionVO() {
    }

    public DevopsEnvUserPermissionVO(Long iamUserId, String loginName, String realName) {
        super(iamUserId, loginName, realName);
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }
}
