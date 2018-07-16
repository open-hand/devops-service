package io.choerodon.devops.domain.application.repository;

import java.util.List;
import java.util.Map;

import io.choerodon.core.domain.Page;
import io.choerodon.devops.api.dto.TagDTO;
import io.choerodon.devops.domain.application.entity.DevopsBranchE;
import io.choerodon.devops.domain.application.entity.gitlab.CommitE;
import io.choerodon.devops.infra.dataobject.DevopsBranchDO;
import io.choerodon.devops.infra.dataobject.gitlab.BranchDO;
import io.choerodon.devops.infra.dataobject.gitlab.TagDO;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;

/**
 * Creator: Runge
 * Date: 2018/7/2
 * Time: 14:03
 * Description:
 */
public interface DevopsGitRepository {

    void createTag(Integer gitLabProjectId, String tag, String ref, Integer userId);

    void deleteTag(Integer gitLabProjectId, String tag, Integer userId);

    Integer getGitLabId(Long applicationId);

    Integer getGitlabUserId();

    Long getUserIdByGitlabUserId(Long gitLabUserId);

    String getGitlabUrl(Long projectId, Long appId);

    void createDevopsBranch(DevopsBranchE devopsBranchE);

    BranchDO createBranch(Integer projectId, String branchName, String baseBranch, Integer userId);

    List<BranchDO> listGitLabBranches(Integer projectId, String path, Integer userId);

    List<DevopsBranchDO> listBranches(Long appId);

    void deleteBranch(Integer projectId, String branchName, Integer userId);

    void deleteDevopsBranch(Long appId, String branchName);

    Page<TagDTO> getTags(Long appId, String path, Integer page, String tagName, Integer size, Integer userId);

    List<TagDO> getTagList(Long appId, Integer userId);

    List<TagDO> getGitLabTags(Integer projectId, Integer userId);

    DevopsBranchE queryByAppAndBranchName(Long appId, String branchName);

    void updateBranchIssue(Long appId, DevopsBranchE devopsBranchE);

    void updateBranchLastCommit(DevopsBranchE devopsBranchE);

    List<DevopsBranchE> listDevopsBranchesByAppId(Long appId);

    List<DevopsBranchE> listDevopsBranchesByAppIdAndBranchName(Long appId, String branchName);

    Map<String, Object> getMergeRequestList(Long projectId,
                                            Integer gitLabProjectId,
                                            String state,
                                            PageRequest pageRequest);

    DevopsBranchE queryByBranchNameAndCommit(String branchName, String commit);

    CommitE getCommit(Integer gitLabProjectId, String commit, Integer userId);
}
