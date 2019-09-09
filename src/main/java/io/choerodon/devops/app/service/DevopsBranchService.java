package io.choerodon.devops.app.service;

import java.util.List;

import com.github.pagehelper.PageInfo;
import io.choerodon.base.domain.PageRequest;
import io.choerodon.devops.infra.dto.DevopsBranchDTO;

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

    PageInfo<DevopsBranchDTO> basePageBranch(Long appServiceId, PageRequest pageRequest, String params);

    void baseDelete(Long appServiceId, String branchName);

    List<DevopsBranchDTO> baseListByAppId(Long appServiceId);

    List<DevopsBranchDTO> baseListByAppIdAndBranchName(Long appServiceId, String branchName);

    DevopsBranchDTO baseQueryByBranchNameAndCommit(String branchName, String commit);

}
