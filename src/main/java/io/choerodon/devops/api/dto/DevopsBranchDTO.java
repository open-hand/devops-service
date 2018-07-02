package io.choerodon.devops.api.dto;


public class DevopsBranchDTO {

    private Long userId;
    private String originBranch;
    private Long issueId;
    private String branchName;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

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
}
