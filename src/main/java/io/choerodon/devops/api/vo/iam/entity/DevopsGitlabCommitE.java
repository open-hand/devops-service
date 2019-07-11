package io.choerodon.devops.api.vo.iam.entity;

import java.util.Date;

public class DevopsGitlabCommitE {

    private Long id;
    private Long appId;
    private Long userId;
    private String commitSha;
    private String commitContent;
    private String ref;
    private Date commitDate;
    private String appName;
    private String url;

    public DevopsGitlabCommitE() {
    }

    public DevopsGitlabCommitE(Long id) {
        this.id = id;
    }

    public DevopsGitlabCommitE(Long id, String ref, String sha, String commitContent, Long userId) {
        this.id = id;
        this.userId = userId;
        this.commitContent = commitContent;
        this.ref = ref;
        this.commitSha = sha;
    }

    public DevopsGitlabCommitE(Long id, String sha, String ref, String content, Long userId, String appName) {
        this.id = id;
        this.commitSha = sha;
        this.commitContent = content;
        this.ref = ref;
        this.userId = userId;
        this.appName = appName;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getAppId() {
        return appId;
    }

    public void setAppId(Long appId) {
        this.appId = appId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getCommitSha() {
        return commitSha;
    }

    public void setCommitSha(String commitSha) {
        this.commitSha = commitSha;
    }

    public String getCommitContent() {
        return commitContent;
    }

    public void setCommitContent(String commitContent) {
        this.commitContent = commitContent;
    }

    public String getRef() {
        return ref;
    }

    public void setRef(String ref) {
        this.ref = ref;
    }

    public Date getCommitDate() {
        return commitDate;
    }

    public void setCommitDate(Date commitDate) {
        this.commitDate = commitDate;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
