package io.choerodon.devops.domain.application.entity;

/**
 * Created by Zenger on 2018/3/28.
 */
public class UserAttrE {

    private Long iamUserId;
    private Long gitlabUserId;
    private String gitlabToken;

    public UserAttrE() {

    }

    public UserAttrE(Long id, Long gitlabUserId) {
        this.iamUserId = id;
        this.gitlabUserId = gitlabUserId;
    }

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
}
