package io.choerodon.devops.app.service;

import java.util.List;

import com.github.pagehelper.PageInfo;
import io.choerodon.base.domain.PageRequest;
import io.choerodon.devops.infra.dto.DevopsMergeRequestDTO;

/**
 * Created by Sheep on 2019/7/15.
 */
public interface DevopsMergeRequestService {

    List<DevopsMergeRequestDTO> baseListBySourceBranch(String sourceBranchName, Long gitLabProjectId);

    DevopsMergeRequestDTO baseQueryByAppIdAndMergeRequestId(Long projectId, Long gitlabMergeRequestId);

    PageInfo<DevopsMergeRequestDTO> basePageByOptions(Integer gitlabProjectId, String state, PageRequest pageRequest);

    List<DevopsMergeRequestDTO> baseQueryByGitlabProjectId(Integer gitlabProjectId);

    Integer baseUpdate(DevopsMergeRequestDTO devopsMergeRequestDTO);

    void baseCreate(DevopsMergeRequestDTO devopsMergeRequestDTO);

    DevopsMergeRequestDTO baseCountMergeRequest(Integer gitlabProjectId);
}
