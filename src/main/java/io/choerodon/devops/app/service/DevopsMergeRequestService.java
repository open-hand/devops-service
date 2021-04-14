package io.choerodon.devops.app.service;

import java.util.List;

import io.choerodon.core.domain.Page;
import io.choerodon.devops.api.vo.DevopsMergeRequestVO;
import io.choerodon.devops.infra.dto.DevopsMergeRequestDTO;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;

/**
 * Created by Sheep on 2019/7/15.
 */
public interface DevopsMergeRequestService {

    List<DevopsMergeRequestDTO> baseListBySourceBranch(String sourceBranchName, Long gitLabProjectId);

    DevopsMergeRequestDTO baseQueryByAppIdAndMergeRequestId(Long projectId, Long gitlabMergeRequestId);

    Page<DevopsMergeRequestDTO> basePageByOptions(Integer gitlabProjectId, String state, PageRequest pageable);

    List<DevopsMergeRequestDTO> baseQueryByGitlabProjectId(Integer gitlabProjectId);

    Integer baseUpdate(DevopsMergeRequestDTO devopsMergeRequestDTO);

    void create(DevopsMergeRequestVO devopsMergeRequestVO, String token);

    void baseCreate(DevopsMergeRequestVO devopsMergeRequestVO, String token);

    DevopsMergeRequestDTO baseCountMergeRequest(Integer gitlabProjectId);
}
