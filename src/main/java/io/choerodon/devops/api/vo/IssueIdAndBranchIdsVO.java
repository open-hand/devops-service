package io.choerodon.devops.api.vo;

import java.util.List;

import io.swagger.annotations.ApiModelProperty;

public class IssueIdAndBranchIdsVO {
    @ApiModelProperty("敏捷issueId")
    private Long issueId;
    @ApiModelProperty("分支列表")
    private List<DevopsBranchVO> branches;

    public Long getIssueId() {
        return issueId;
    }

    public void setIssueId(Long issueId) {
        this.issueId = issueId;
    }

    public List<DevopsBranchVO> getBranches() {
        return branches;
    }

    public void setBranches(List<DevopsBranchVO> branches) {
        this.branches = branches;
    }
}
