package io.choerodon.devops.domain.application.repository;

import java.util.List;

import com.github.pagehelper.PageInfo;
import io.choerodon.base.domain.PageRequest;
import io.choerodon.devops.domain.application.entity.DevopsMergeRequestE;


public interface DevopsMergeRequestRepository {

    Integer create(DevopsMergeRequestE devopsMergeRequestE);

    DevopsMergeRequestE queryByAppIdAndGitlabId(Long applicationId, Long gitlabMergeRequestId);

    List<DevopsMergeRequestE> getBySourceBranch(String sourceBranchName, Long gitLabProjectId);

    Integer update(DevopsMergeRequestE devopsMergeRequestE);

    PageInfo<DevopsMergeRequestE> getMergeRequestList(Integer gitlabProjectId, String state, PageRequest pageRequest);

    PageInfo<DevopsMergeRequestE> getByGitlabProjectId(Integer gitlabProjectId, PageRequest pageRequest);

    List<DevopsMergeRequestE> getByGitlabProjectId(Integer gitlabProjectId);

    void saveDevopsMergeRequest(DevopsMergeRequestE devopsMergeRequestE);
}
