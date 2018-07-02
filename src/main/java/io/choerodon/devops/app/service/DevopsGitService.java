package io.choerodon.devops.app.service;

import io.choerodon.devops.api.dto.DevopsBranchDTO;

/**
 * Creator: Runge
 * Date: 2018/7/2
 * Time: 14:39
 * Description:
 */
public interface DevopsGitService {
    void createTag(Long projectId, Long appId, String tag, String ref);

    /**
     * 创建分支
     *
     * @param projectId       项目ID
     * @param applicationId   应用ID
     * @param devopsBranchDTO 分支
     */
    void createBranch(Long projectId, Long applicationId, DevopsBranchDTO devopsBranchDTO);
}
