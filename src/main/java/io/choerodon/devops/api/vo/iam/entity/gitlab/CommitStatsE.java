package io.choerodon.devops.api.vo.iam.entity.gitlab;

/**
 * GitLab 提交状态
 */
public class CommitStatsE {

    private Integer additions;
    private Integer deletions;
    private Integer total;

    public Integer getAdditions() {
        return additions;
    }

    public void setAdditions(Integer additions) {
        this.additions = additions;
    }

    public Integer getDeletions() {
        return deletions;
    }

    public void setDeletions(Integer deletions) {
        this.deletions = deletions;
    }

    public Integer getTotal() {
        return total;
    }

    public void setTotal(Integer total) {
        this.total = total;
    }
}
