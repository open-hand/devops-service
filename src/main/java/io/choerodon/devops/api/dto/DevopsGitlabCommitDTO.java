package io.choerodon.devops.api.dto;

import java.util.Date;
import java.util.List;

import io.choerodon.core.domain.Page;

/**
 * Created by n!Ck
 * Date: 2018/9/19
 * Time: 15:09
 * Description:
 */
public class DevopsGitlabCommitDTO {
    private List<CommitFormUserDTO> commitFormUserDTOList;
    private List<Date> totalCommitsDate;
    private Page<CommitFormRecordDTO> commitFormRecordDTOPage;

    public DevopsGitlabCommitDTO() {
    }

    public DevopsGitlabCommitDTO(List<CommitFormUserDTO> commitFormUserDTOList,
                                 List<Date> totalCommitsDate,
                                 Page<CommitFormRecordDTO> commitFormRecordDTOPage) {
        this.commitFormUserDTOList = commitFormUserDTOList;
        this.totalCommitsDate = totalCommitsDate;
        this.commitFormRecordDTOPage = commitFormRecordDTOPage;
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

    public Page<CommitFormRecordDTO> getCommitFormRecordDTOPage() {
        return commitFormRecordDTOPage;
    }

    public void setCommitFormRecordDTOPage(Page<CommitFormRecordDTO> commitFormRecordDTOPage) {
        this.commitFormRecordDTOPage = commitFormRecordDTOPage;
    }

    public void setTotalCommitsDate(List<Date> totalCommitsDate) {

        this.totalCommitsDate = totalCommitsDate;
    }
}
