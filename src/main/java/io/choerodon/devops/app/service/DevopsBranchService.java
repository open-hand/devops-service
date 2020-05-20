package io.choerodon.devops.app.service;

import java.util.List;

import com.github.pagehelper.PageInfo;
import org.springframework.data.domain.Pageable;
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

    PageInfo<DevopsBranchDTO> basePageBranch(Long appServiceId, Pageable pageable, String params);

    void baseDelete(Long appServiceId, String branchName);

    void deleteAllBaranch(Long appServiceId);
}
