package io.choerodon.devops.app.service;

import java.util.List;

import io.choerodon.core.domain.Page;
import io.choerodon.devops.api.dto.BranchDTO;
import io.choerodon.devops.api.dto.DevopsBranchDTO;
import io.choerodon.devops.api.dto.MergeRequestDTO;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;

/**
 * Creator: Runge
 * Date: 2018/7/2
 * Time: 14:39
 * Description:
 */
public interface DevopsGitService {

    String getUrl(Long projectId, Long appId);

    void createTag(Long projectId, Long appId, String tag, String ref);

    /**
     * 创建分支
     *
     * @param projectId       项目ID
     * @param applicationId   应用ID
     * @param devopsBranchDTO 分支
     */
    void createBranch(Long projectId, Long applicationId, DevopsBranchDTO devopsBranchDTO);

    /**
     * 获取工程下所有分支名
     *
     * @param projectId     项目 ID
     * @param applicationId 应用ID
     * @return Branch 列表
     */
    List<BranchDTO> listBranches(Long projectId, Long applicationId);

    /**
     * 查询单个分支
     *
     * @param projectId     项目 ID
     * @param applicationId 应用ID
     * @param branchName    分支名
     * @return BranchUpdateDTO
     */
    DevopsBranchDTO queryBranch(Long projectId, Long applicationId, String branchName);

    /**
     * 更新分支关联的问题
     *
     * @param projectId       项目 ID
     * @param applicationId   应用ID
     * @param devopsBranchDTO 分支
     */
    void updateBranch(Long projectId, Long applicationId, DevopsBranchDTO devopsBranchDTO);

    /**
     * 删除分支
     *
     * @param projectId     项目 ID
     * @param applicationId 应用ID
     * @param branchName    分支名
     */
    void deleteBranch(Long projectId, Long applicationId, String branchName);

    Page<MergeRequestDTO> getMergeRequestList(Long projectId, Long aplicationId, String state, PageRequest pageRequest);

}
