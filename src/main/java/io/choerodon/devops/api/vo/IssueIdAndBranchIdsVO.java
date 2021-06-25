package io.choerodon.devops.api.vo;

import java.util.List;

public class IssueIdAndBranchIdsVO {
    private Long issueId;
    private List<Long> branchIds;

    public Long getIssueId() {
        return issueId;
    }

    public void setIssueId(Long issueId) {
        this.issueId = issueId;
    }

    public List<Long> getBranchIds() {
        return branchIds;
    }

    public void setBranchIds(List<Long> branchIds) {
        this.branchIds = branchIds;
    }
}
