package io.choerodon.devops.app.service;

import java.util.List;
import java.util.Set;

import io.choerodon.core.domain.Page;
import io.choerodon.devops.infra.dto.AppServiceDTO;
import io.choerodon.devops.infra.dto.DevopsBranchDTO;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;

/**
 * Created by Sheep on 2019/7/11.
 */
public interface DevopsBranchService {
    List<DevopsBranchDTO> baseListDevopsBranchesByIssueId(Long issueId);

    DevopsBranchDTO baseQueryByAppAndBranchName(Long appServiceId, String branchName);

    DevopsBranchDTO baseQueryByAppAndBranchNameWithIssueIds(Long appServiceId, String branchName);

    DevopsBranchDTO baseQueryByAppAndBranchIdWithIssueId(Long appServiceId, Long branchId);

    void updateBranchIssue(Long projectId, AppServiceDTO appServiceDTO, DevopsBranchDTO devopsBranchDTO, boolean onlyInsert);

    void baseUpdateBranchLastCommit(DevopsBranchDTO devopsBranchDTO);

    DevopsBranchDTO baseCreate(DevopsBranchDTO devopsBranchDTO);

    DevopsBranchDTO baseQuery(Long devopsBranchId);

    void baseUpdateBranch(DevopsBranchDTO devopsBranchDTO);

    Page<DevopsBranchDTO> basePageBranch(Long appServiceId, PageRequest pageable, String params, Long issueId);

    void baseDelete(Long appServiceId, String branchName);

    void deleteAllBranch(Long appServiceId);

    List<DevopsBranchDTO> listByCommitIs(List<Long> commitIds);

    List<Long> listDeletedBranchIds(Set<Long> collect);

    List<DevopsBranchDTO> listByIds(List<Long> branchIds);

    /**
     * 查询工作项是否与分支有关联关系
     *
     * @param projectId
     * @param issueId
     * @return
     */
    Boolean checkIssueBranchRelExist(Long projectId, Long issueId);

    /**
     * 复制工作项与分支关联关系
     * @param projectId
     * @param oldIssueId
     * @param newIssueId
     */
    void copyIssueBranchRel(Long projectId, Long oldIssueId, Long newIssueId);
}