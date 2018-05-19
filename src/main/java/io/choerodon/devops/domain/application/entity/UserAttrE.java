package io.choerodon.devops.domain.application.entity;

/**
 * Created by Zenger on 2018/3/28.
 */
public class UserAttrE {

    private Long id;
    private Long gitlabUserId;

    public UserAttrE() {

    }

    public UserAttrE(Long id, Long gitlabUserId) {
        this.id = id;
        this.gitlabUserId = gitlabUserId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getGitlabUserId() {
        return gitlabUserId;
    }

    public void setGitlabUserId(Long gitlabUserId) {
        this.gitlabUserId = gitlabUserId;
    }
}
