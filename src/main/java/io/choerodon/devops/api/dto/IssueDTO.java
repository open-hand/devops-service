package io.choerodon.devops.api.dto;

import java.util.Date;

public class IssueDTO {

    private String mergeRequestStatus;

    private Date mergeRequestUpdateTime;

    private Date commitUpdateTime;

    private Integer branchCount;

    private Integer totalCommit;

    private Integer totalMergeRequest;

    public String getMergeRequestStatus() {
        return mergeRequestStatus;
    }

    public void setMergeRequestStatus(String mergeRequestStatus) {
        this.mergeRequestStatus = mergeRequestStatus;
    }

    public Date getMergeRequestUpdateTime() {
        return mergeRequestUpdateTime;
    }

    public void setMergeRequestUpdateTime(Date mergeRequestUpdateTime) {
        this.mergeRequestUpdateTime = mergeRequestUpdateTime;
    }

    public Date getCommitUpdateTime() {
        return commitUpdateTime;
    }

    public void setCommitUpdateTime(Date commitUpdateTime) {
        this.commitUpdateTime = commitUpdateTime;
    }

    public Integer getBranchCount() {
        return branchCount;
    }

    public void setBranchCount(Integer branchCount) {
        this.branchCount = branchCount;
    }

    public Integer getTotalCommit() {
        return totalCommit;
    }

    public void setTotalCommit(Integer totalCommit) {
        this.totalCommit = totalCommit;
    }

    public Integer getTotalMergeRequest() {
        return totalMergeRequest;
    }

    public void setTotalMergeRequest(Integer totalMergeRequest) {
        this.totalMergeRequest = totalMergeRequest;
    }
}
