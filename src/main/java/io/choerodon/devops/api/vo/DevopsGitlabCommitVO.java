package io.choerodon.devops.api.vo;

import java.util.Date;
import java.util.List;

/**
 * Created by n!Ck
 * Date: 2018/9/19
 * Time: 15:09
 * Description:
 */
public class DevopsGitlabCommitVO {
    private List<CommitFormUserVO> commitFormUserVOList;
    private List<Date> totalCommitsDate;

    public DevopsGitlabCommitVO() {
    }

    public DevopsGitlabCommitVO(List<CommitFormUserVO> commitFormUserVOList,
                                List<Date> totalCommitsDate) {
        this.commitFormUserVOList = commitFormUserVOList;
        this.totalCommitsDate = totalCommitsDate;
    }

    public List<CommitFormUserVO> getCommitFormUserVOList() {
        return commitFormUserVOList;
    }

    public void setCommitFormUserVOList(List<CommitFormUserVO> commitFormUserVOList) {
        this.commitFormUserVOList = commitFormUserVOList;
    }

    public List<Date> getTotalCommitsDate() {
        return totalCommitsDate;
    }

    public void setTotalCommitsDate(List<Date> totalCommitsDate) {

        this.totalCommitsDate = totalCommitsDate;
    }
}
