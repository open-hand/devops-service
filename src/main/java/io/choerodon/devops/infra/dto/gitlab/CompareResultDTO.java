package io.choerodon.devops.infra.dto.gitlab;

import java.util.List;

import io.choerodon.devops.infra.dto.CommitDTO;

/**
 * Creator: Runge
 * Date: 2018/7/26
 * Time: 16:30
 * Description:
 */
public class CompareResultDTO {
    private CommitDTO commit;
    private List<CommitDTO> commits;
    private List<DiffDTO> diffs;
    private Boolean compareTimeout;
    private Boolean compareSameRef;

    public CommitDTO getCommit() {
        return commit;
    }

    public void setCommit(CommitDTO commit) {
        this.commit = commit;
    }

    public List<CommitDTO> getCommits() {
        return commits;
    }

    public void setCommits(List<CommitDTO> commits) {
        this.commits = commits;
    }

    public List<DiffDTO> getDiffs() {
        return diffs;
    }

    public void setDiffs(List<DiffDTO> diffs) {
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
