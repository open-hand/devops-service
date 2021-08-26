package io.choerodon.devops.api.vo;

import java.util.List;

public class IssueIdAndBranchIdsVO {
    private Long issueId;
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
