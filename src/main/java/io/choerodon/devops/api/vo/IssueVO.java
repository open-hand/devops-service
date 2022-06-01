package io.choerodon.devops.api.vo;

import java.util.Date;

import io.swagger.annotations.ApiModelProperty;

public class IssueVO {

    @ApiModelProperty("合并请求状态")
    private String mergeRequestStatus;
    @ApiModelProperty("合并请求更新时间")
    private Date mergeRequestUpdateTime;
    @ApiModelProperty("提交更新时间")
    private Date commitUpdateTime;
    @ApiModelProperty("分支数")
    private Integer branchCount;
    @ApiModelProperty("总提交数")
    private Integer totalCommit;
    @ApiModelProperty("总合并请求数")
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
