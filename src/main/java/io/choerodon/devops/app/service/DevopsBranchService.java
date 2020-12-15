package io.choerodon.devops.app.service;

import java.util.List;

import io.choerodon.core.domain.Page;
import io.choerodon.devops.infra.dto.DevopsBranchDTO;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;

/**
 * Created by Sheep on 2019/7/11.
 */
public interface DevopsBranchService {
    List<DevopsBranchDTO> baseListDevopsBranchesByIssueId(Long issueId);

    DevopsBranchDTO baseQueryByAppAndBranchName(Long appServiceId, String branchName);

    void updateBranchIssue(DevopsBranchDTO devopsBranchDTO);

    void baseUpdateBranchLastCommit(DevopsBranchDTO devopsBranchDTO);

    DevopsBranchDTO baseCreate(DevopsBranchDTO devopsBranchDTO);

    DevopsBranchDTO baseQuery(Long devopsBranchId);

    void baseUpdateBranch(DevopsBranchDTO devopsBranchDTO);

    Page<DevopsBranchDTO> basePageBranch(Long appServiceId, PageRequest pageable, String params, Long issueId);

    void baseDelete(Long appServiceId, String branchName);

    void deleteAllBaranch(Long appServiceId);
}
