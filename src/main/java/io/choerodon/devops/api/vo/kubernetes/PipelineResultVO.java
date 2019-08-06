package io.choerodon.devops.api.vo.kubernetes;

import java.util.List;

import io.choerodon.devops.api.vo.GitlabJobVO;


public class PipelineResultVO {

    private Long id;
    private String status;
    private String createUser;
    private String sha;
    private String ref;
    private String createdAt;
    private Long[] time;
    private String appServiceName;
    private String appServiceCode;
    private Boolean appServiceStatus;
    private Long gitlabProjectId;
    private String imageUrl;
    private String gitlabUrl;
    private Boolean latest;
    private List<GitlabJobVO> jobs;

    public String getAppServiceName() {
        return appServiceName;
    }

    public void setAppServiceName(String appServiceName) {
        this.appServiceName = appServiceName;
    }

    public String getAppServiceCode() {
        return appServiceCode;
    }

    public void setAppServiceCode(String appServiceCode) {
        this.appServiceCode = appServiceCode;
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

    public Boolean getAppServiceStatus() {
        return appServiceStatus;
    }

    public void setAppServiceStatus(Boolean appServiceStatus) {
        this.appServiceStatus = appServiceStatus;
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

    public List<GitlabJobVO> getJobs() {
        return jobs;
    }

    public void setJobs(List<GitlabJobVO> jobs) {
        this.jobs = jobs;
    }

}

