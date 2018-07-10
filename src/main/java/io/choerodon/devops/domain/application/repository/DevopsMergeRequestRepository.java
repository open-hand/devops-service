package io.choerodon.devops.domain.application.repository;

import java.util.List;

import io.choerodon.devops.domain.application.entity.DevopsMergeRequestE;
import io.choerodon.devops.domain.application.entity.gitlab.MergeRequestE;

public interface DevopsMergeRequestRepository {

    Integer create(DevopsMergeRequestE devopsMergeRequestE);

    Integer queryByAppIdAndGitlabId(Long applicationId, Long gitlabMergeRequestId);

    List<MergeRequestE> getBySourceBranch(String sourceBranchName, Long gitLabProjectId);
}
