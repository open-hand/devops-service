package io.choerodon.devops.domain.application.repository;

import java.util.Date;
import java.util.List;
import java.util.Map;

import com.github.pagehelper.PageInfo;
import io.choerodon.base.domain.PageRequest;
import io.choerodon.devops.api.vo.CommitFormRecordDTO;
import io.choerodon.devops.domain.application.entity.DevopsGitlabCommitE;
import io.choerodon.devops.domain.application.entity.iam.UserE;

public interface DevopsGitlabCommitRepository {

    DevopsGitlabCommitE create(DevopsGitlabCommitE devopsGitlabCommitE);

    DevopsGitlabCommitE queryByShaAndRef(String sha, String ref);

    List<DevopsGitlabCommitE> listCommits(Long projectId, List<Long> appIds, Date startDate, Date endDate);

    PageInfo<CommitFormRecordDTO> pageCommitRecord(Long projectId, List<Long> appId,
                                                   PageRequest pageRequest, Map<Long, UserE> userMap,
                                                   Date startDate, Date endDate);

    void update(DevopsGitlabCommitE devopsGitlabCommitE);

    List<DevopsGitlabCommitE> queryByAppIdAndBranch(Long appId, String branch, Date startDate);

}
