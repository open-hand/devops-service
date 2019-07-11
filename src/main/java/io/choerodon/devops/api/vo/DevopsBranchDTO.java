package io.choerodon.devops.api.vo;

import java.util.List;

import io.choerodon.devops.domain.application.entity.DevopsGitlabCommitE;

public class DevopsBranchDTO {

    private Long appId;
    private String appName;
    private String originBranch;
    private Long issueId;
    private String branchName;
    private List<DevopsGitlabCommitE> commits;
    private List<CustomMergeRequestDTO> mergeRequests;

    public String getOriginBranch() {
        return originBranch;
    }

    public void setOriginBranch(String originBranch) {
        this.originBranch = originBranch;
    }

    public String getBranchName() {
        return branchName;
    }

    public void setBranchName(String branchName) {
        this.branchName = branchName;
    }

    public Long getIssueId() {
        return issueId;
    }

    public void setIssueId(Long issueId) {
        this.issueId = issueId;
    }

    public Long getAppId() {
        return appId;
    }

    public void setAppId(Long appId) {
        this.appId = appId;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public List<DevopsGitlabCommitE> getCommits() {
        return commits;
    }

    public void setCommits(List<DevopsGitlabCommitE> commits) {
        this.commits = commits;
    }

    public List<CustomMergeRequestDTO> getMergeRequests() {
        return mergeRequests;
    }

    public void setMergeRequests(List<CustomMergeRequestDTO> mergeRequests) {
        this.mergeRequests = mergeRequests;
    }
}
