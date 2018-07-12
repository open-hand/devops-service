package io.choerodon.devops.domain.application.repository;

import java.util.List;

import io.choerodon.core.domain.Page;
import io.choerodon.devops.domain.application.entity.DevopsMergeRequestE;
import io.choerodon.devops.domain.application.entity.gitlab.MergeRequestE;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;

public interface DevopsMergeRequestRepository {

    Integer create(DevopsMergeRequestE devopsMergeRequestE);

    DevopsMergeRequestE queryByAppIdAndGitlabId(Long applicationId, Long gitlabMergeRequestId);

    List<DevopsMergeRequestE> getBySourceBranch(String sourceBranchName, Long gitLabProjectId);

    Integer update(DevopsMergeRequestE devopsMergeRequestE);

    Page<DevopsMergeRequestE> getMergeRequestList(Integer gitlabProjectId, String state, PageRequest pageRequest);

    Page<DevopsMergeRequestE> getByGitlabProjectId(Integer gitlabProjectId, PageRequest pageRequest);

    List<DevopsMergeRequestE> getByGitlabProjectId(Integer gitlabProjectId);
}
