package io.choerodon.devops.infra.dto;

import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import io.swagger.annotations.ApiModelProperty;

import io.choerodon.mybatis.annotation.ModifyAudit;
import io.choerodon.mybatis.annotation.VersionAudit;
import io.choerodon.mybatis.domain.AuditDomain;

/**
 * Created by zzy on 2018/3/26.
 */
@ModifyAudit
@VersionAudit
@Table(name = "devops_user")
public class UserAttrDTO extends AuditDomain {
    @Id
    private Long iamUserId;

    @NotNull
    private Long gitlabUserId;

    private String gitlabToken;

    private String gitlabUserName;

    @ApiModelProperty("用户是否是gitlab的admin")
    private Boolean isGitlabAdmin;

    public Long getIamUserId() {
        return iamUserId;
    }

    public void setIamUserId(Long iamUserId) {
        this.iamUserId = iamUserId;
    }

    public Long getGitlabUserId() {
        return gitlabUserId;
    }

    public void setGitlabUserId(Long gitlabUserId) {
        this.gitlabUserId = gitlabUserId;
    }

    public String getGitlabToken() {
        return gitlabToken;
    }

    public void setGitlabToken(String gitlabToken) {
        this.gitlabToken = gitlabToken;
    }

    public String getGitlabUserName() {
        return gitlabUserName;
    }

    public void setGitlabUserName(String gitlabUserName) {
        this.gitlabUserName = gitlabUserName;
    }

    public Boolean getGitlabAdmin() {
        return isGitlabAdmin;
    }

    public void setGitlabAdmin(Boolean gitlabAdmin) {
        isGitlabAdmin = gitlabAdmin;
    }

    @Override
    public String toString() {
        return "UserAttrDTO{" +
                "iamUserId=" + iamUserId +
                ", gitlabUserId=" + gitlabUserId +
                ", gitlabToken='" + gitlabToken + '\'' +
                ", gitlabUserName='" + gitlabUserName + '\'' +
                ", isGitlabAdmin=" + isGitlabAdmin +
                '}';
    }
}
