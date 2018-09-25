package io.choerodon.devops.domain.application.repository;

import java.util.List;
import java.util.Map;

import io.choerodon.core.domain.Page;
import io.choerodon.devops.api.dto.CommitFormRecordDTO;
import io.choerodon.devops.domain.application.entity.DevopsGitlabCommitE;
import io.choerodon.devops.domain.application.entity.iam.UserE;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;

public interface DevopsGitlabCommitRepository {

    DevopsGitlabCommitE create(DevopsGitlabCommitE devopsGitlabCommitE);

    DevopsGitlabCommitE queryBySha(String sha);

    List<DevopsGitlabCommitE> listCommits(Long projectId, List<Long> appIds, String startDate, String endDate);

    Page<CommitFormRecordDTO> pageCommitRecord(Long projectId, List<Long> appId,
                                               PageRequest pageRequest, Map<Long, UserE> userMap,
                                               String startDate, String endDate);

}
