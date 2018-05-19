package io.choerodon.devops.domain.application.event;

/**
 * Created by zzy on 2018/3/23.
 */
public class GitLabMemberRoleEvent {

    private Long gitlabGroupId;
    private Integer gitlabUserId;
    private String objectKind;
    private Integer accessLevel;
    private String changeType;

    public Long getGitlabGroupId() {
        return gitlabGroupId;
    }

    public void setGitlabGroupId(Long gitlabGroupId) {
        this.gitlabGroupId = gitlabGroupId;
    }

    public Integer getGitlabUserId() {
        return gitlabUserId;
    }

    public void setGitlabUserId(Integer gitlabUserId) {
        this.gitlabUserId = gitlabUserId;
    }

    public String getObjectKind() {
        return objectKind;
    }

    public void setObjectKind(String objectKind) {
        this.objectKind = objectKind;
    }

    public Integer getAccessLevel() {
        return accessLevel;
    }

    public void setAccessLevel(Integer accessLevel) {
        this.accessLevel = accessLevel;
    }

    public String getChangeType() {
        return changeType;
    }

    public void setChangeType(String changeType) {
        this.changeType = changeType;
    }
}
