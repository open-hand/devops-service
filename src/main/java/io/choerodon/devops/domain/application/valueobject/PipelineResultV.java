package io.choerodon.devops.domain.application.valueobject;

import java.util.List;

import io.choerodon.devops.api.vo.iam.entity.gitlab.GitlabJobE;


public class PipelineResultV {

    private Long id;
    private String status;
    private String createUser;
    private String sha;
    private String ref;
    private String createdAt;
    private Long[] time;
    private String appName;
    private String appCode;
    private Boolean appStatus;
    private Long gitlabProjectId;
    private String imageUrl;
    private String gitlabUrl;
    private Boolean latest;
    private List<GitlabJobE> jobs;

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getAppCode() {
        return appCode;
    }

    public void setAppCode(String appCode) {
        this.appCode = appCode;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCreateUser() {
        return createUser;
    }

    public void setCreateUser(String createUser) {
        this.createUser = createUser;
    }

    public String getRef() {
        return ref;
    }

    public void setRef(String ref) {
        this.ref = ref;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public Long[] getTime() {
        return time;
    }

    public void setTime(Long[] time) {
        this.time = time;
    }

    public Long getGitlabProjectId() {
        return gitlabProjectId;
    }

    public void setGitlabProjectId(Long gitlabProjectId) {
        this.gitlabProjectId = gitlabProjectId;
    }

    public Boolean getAppStatus() {
        return appStatus;
    }

    public void setAppStatus(Boolean appStatus) {
        this.appStatus = appStatus;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getGitlabUrl() {
        return gitlabUrl;
    }

    public void setGitlabUrl(String gitlabUrl) {
        this.gitlabUrl = gitlabUrl;
    }

    public String getSha() {
        return sha;
    }

    public void setSha(String sha) {
        this.sha = sha;
    }

    public Boolean getLatest() {
        return latest;
    }

    public void setLatest(Boolean latest) {
        this.latest = latest;
    }

    public List<GitlabJobE> getJobs() {
        return jobs;
    }

    public void setJobs(List<GitlabJobE> jobs) {
        this.jobs = jobs;
    }

}

