package io.choerodon.devops.domain.application.repository;

import java.util.Date;
import java.util.List;
import java.util.Map;

import io.choerodon.core.domain.Page;
import io.choerodon.devops.api.dto.CommitFormRecordDTO;
import io.choerodon.devops.domain.application.entity.DevopsGitlabCommitE;
import io.choerodon.devops.domain.application.entity.iam.UserE;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;

public interface DevopsGitlabCommitRepository {

    DevopsGitlabCommitE create(DevopsGitlabCommitE devopsGitlabCommitE);

    DevopsGitlabCommitE queryByShaAndRef(String sha, String ref);

    List<DevopsGitlabCommitE> listCommits(Long projectId, List<Long> appIds, Date startDate, Date endDate);

    Page<CommitFormRecordDTO> pageCommitRecord(Long projectId, List<Long> appId,
                                               PageRequest pageRequest, Map<Long, UserE> userMap,
                                               Date startDate, Date endDate);

    void update(DevopsGitlabCommitE devopsGitlabCommitE);

    List<DevopsGitlabCommitE> queryByAppIdAndBranch(Long appId, String branch, Date startDate);

}
