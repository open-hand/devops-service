package io.choerodon.devops.domain.application.repository;

import java.util.List;

import com.github.pagehelper.PageInfo;
import io.choerodon.base.domain.PageRequest;
import io.choerodon.devops.infra.dto.DevopsMergeRequestDTO;


public interface DevopsMergeRequestRepository {


    DevopsMergeRequestDTO baseQueryByAppIdAndMergeRequestId(Long applicationId, Long gitlabMergeRequestId);

    List<DevopsMergeRequestDTO> baseQueryBySourceBranch(String sourceBranchName, Long gitLabProjectId);

    Integer baseUpdate(DevopsMergeRequestDTO devopsMergeRequestDTO);

    PageInfo<DevopsMergeRequestDTO> basePageByOptions(Integer gitlabProjectId, String state, PageRequest pageRequest);

    List<DevopsMergeRequestDTO> baseQueryByGitlabProjectId(Integer gitlabProjectId);

    void baseCreate(DevopsMergeRequestDTO devopsMergeRequestDTO);
}
