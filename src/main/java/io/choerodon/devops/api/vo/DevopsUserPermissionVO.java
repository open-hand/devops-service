package io.choerodon.devops.api.vo;

import java.util.Date;
import java.util.List;

import io.choerodon.devops.infra.dto.iam.RoleDTO;

/**
 * @author zmf
 */
public class DevopsUserPermissionVO extends DevopsEnvUserVO {
    private List<RoleDTO> roles;
    private Date creationDate;
    private Boolean gitlabProjectOwner;

    public DevopsUserPermissionVO() {
    }

    public DevopsUserPermissionVO(Long iamUserId, String loginName, String realName) {
        super(iamUserId, loginName, realName);
    }

    public DevopsUserPermissionVO(Long iamUserId, String loginName, String realName, Date creationDate) {
        super(iamUserId, loginName, realName);
        this.creationDate = creationDate;
    }

    public DevopsUserPermissionVO(Long iamUserId, String loginName, String realName, String imageUrl) {
        super(iamUserId, loginName, realName, imageUrl);
    }

    public List<RoleDTO> getRoles() {
        return roles;
    }

    public void setRoles(List<RoleDTO> roles) {
        this.roles = roles;
    }

    public Boolean getGitlabProjectOwner() {
        return gitlabProjectOwner;
    }

    public void setGitlabProjectOwner(Boolean gitlabProjectOwner) {
        this.gitlabProjectOwner = gitlabProjectOwner;
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
