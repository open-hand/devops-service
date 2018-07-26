package io.choerodon.devops.domain.application.entity.gitlab;

import java.util.List;

/**
 * Creator: Runge
 * Date: 2018/7/26
 * Time: 16:30
 * Description:
 */
public class CompareResultsE {
    private CommitE commit;
    private List<CommitE> commits;
    private List<DiffE> diffs;
    private Boolean compareTimeout;
    private Boolean compareSameRef;

    public CommitE getCommit() {
        return commit;
    }

    public void setCommit(CommitE commit) {
        this.commit = commit;
    }

    public List<CommitE> getCommits() {
        return commits;
    }

    public void setCommits(List<CommitE> commits) {
        this.commits = commits;
    }

    public List<DiffE> getDiffs() {
        return diffs;
    }

    public void setDiffs(List<DiffE> diffs) {
        this.diffs = diffs;
    }

    public Boolean getCompareTimeout() {
        return compareTimeout;
    }

    public void setCompareTimeout(Boolean compareTimeout) {
        this.compareTimeout = compareTimeout;
    }

    public Boolean getCompareSameRef() {
        return compareSameRef;
    }

    public void setCompareSameRef(Boolean compareSameRef) {
        this.compareSameRef = compareSameRef;
    }
}
