package io.choerodon.devops.api.vo;

import java.util.Date;
import java.util.List;

/**
 * Created by n!Ck
 * Date: 2018/9/19
 * Time: 15:09
 * Description:
 */
public class DevopsGitlabCommitDTO {
    private List<CommitFormUserDTO> commitFormUserDTOList;
    private List<Date> totalCommitsDate;

    public DevopsGitlabCommitDTO() {
    }

    public DevopsGitlabCommitDTO(List<CommitFormUserDTO> commitFormUserDTOList,
                                 List<Date> totalCommitsDate) {
        this.commitFormUserDTOList = commitFormUserDTOList;
        this.totalCommitsDate = totalCommitsDate;
    }

    public List<CommitFormUserDTO> getCommitFormUserDTOList() {
        return commitFormUserDTOList;
    }

    public void setCommitFormUserDTOList(List<CommitFormUserDTO> commitFormUserDTOList) {
        this.commitFormUserDTOList = commitFormUserDTOList;
    }

    public List<Date> getTotalCommitsDate() {
        return totalCommitsDate;
    }

    public void setTotalCommitsDate(List<Date> totalCommitsDate) {

        this.totalCommitsDate = totalCommitsDate;
    }
}
