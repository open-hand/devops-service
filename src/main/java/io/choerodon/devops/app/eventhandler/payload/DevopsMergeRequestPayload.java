package io.choerodon.devops.app.eventhandler.payload;

public class DevopsMergeRequestPayload {
    private String serviceCode;
    private String sourceBranchName;
    private String targetBranchName;
    private Long issueId;
    private Long projectId;

    public String getSourceBranchName() {
        return sourceBranchName;
    }

    public void setSourceBranchName(String sourceBranchName) {
        this.sourceBranchName = sourceBranchName;
    }

    public String getTargetBranchName() {
        return targetBranchName;
    }

    public void setTargetBranchName(String targetBranchName) {
        this.targetBranchName = targetBranchName;
    }

    public Long getIssueId() {
        return issueId;
    }

    public void setIssueId(Long issueId) {
        this.issueId = issueId;
    }

    public String getServiceCode() {
        return serviceCode;
    }

    public void setServiceCode(String serviceCode) {
        this.serviceCode = serviceCode;
    }

    public Long getProjectId() {
        return projectId;
    }

    public DevopsMergeRequestPayload setProjectId(Long projectId) {
        this.projectId = projectId;
        return this;
    }
}
