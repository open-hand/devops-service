package io.choerodon.devops.domain.application.repository;

import java.util.List;

import io.choerodon.devops.domain.application.entity.DevopsMergeRequestE;
import io.choerodon.devops.domain.application.entity.gitlab.MergeRequestE;

public interface DevopsMergeRequestRepository {

    Integer create(DevopsMergeRequestE devopsMergeRequestE);

    DevopsMergeRequestE queryByAppIdAndGitlabId(Long applicationId, Long gitlabMergeRequestId);

    List<DevopsMergeRequestE> getBySourceBranch(String sourceBranchName, Long gitLabProjectId);

    Integer update(DevopsMergeRequestE devopsMergeRequestE);
}
