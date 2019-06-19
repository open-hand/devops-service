package io.choerodon.devops.infra.dataobject;

import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import io.choerodon.mybatis.entity.BaseDTO;

/**
 * Created by zzy on 2018/3/26.
 */
@Table(name = "devops_user")
public class UserAttrDO extends BaseDTO {

    @Id
    private Long iamUserId;

    @NotNull
    private Long gitlabUserId;

    private String gitlabToken;

    private String gitlabUserName;

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
}
