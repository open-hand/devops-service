package io.choerodon.devops.api.vo;

import java.util.Date;

/**
 * @author zmf
 */
public class DevopsUserPermissionVO extends DevopsEnvUserVO {
    private String role;
    private Date creationDate;

    public DevopsUserPermissionVO() {
    }

    public DevopsUserPermissionVO(Long iamUserId, String loginName, String realName) {
        super(iamUserId, loginName, realName);
    }
    public DevopsUserPermissionVO(Long iamUserId, String loginName, String realName,Date creationDate) {
        super(iamUserId, loginName, realName);
        this.creationDate=creationDate;
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

    @Override
    public int hashCode() {
        String in = super.getIamUserId() + super.getLoginName() + super.getRealName();
        return in.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (this.getClass() != obj.getClass()) {
            return false;
        }
        DevopsUserPermissionVO s = (DevopsUserPermissionVO) obj;
        return super.getIamUserId().equals(s.getIamUserId());
    }
}
