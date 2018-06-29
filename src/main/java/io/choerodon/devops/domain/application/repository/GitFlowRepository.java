package io.choerodon.devops.domain.application.repository;

import java.util.List;
import java.util.Optional;

import io.choerodon.devops.infra.dataobject.DevopsMergeRequestDO;
import io.choerodon.devops.infra.dataobject.gitlab.*;

/**
 * Created with IntelliJ IDEA.
 * User: Runge
 * Date: 2018/4/9
 * Time: 14:43
 * Description:
 */
public interface GitFlowRepository {
    Integer getGitLabId(Long applicationId);

    void createMergeRequest(Integer projectId, String sourceBranch, String targetBranch, Integer userId);

    MergeRequestDO getMergeRequest(Integer projectId, Long mergeRequestId, Integer userId);

    void deleteMergeRequest(Integer projectId, Integer mergeRequestId);

    DevopsMergeRequestDO getDevOpsMergeRequest(Integer projectId, String branchName, String targetBranch);

    void deleteDevOpsMergeRequest(Long id);

    void updateMergeRequest(Integer projectId, Long mergeRequestId, Integer userId);

    Boolean checkMergeRequestCommit(Integer projectId, Long mergeRequestId, Integer userId);

    void createBranch(Integer projectId, String branchName, String baseBranch, Integer userId);

    List<BranchDO> listBranches(Integer projectId, String path, Integer userId);

    void deleteBranch(Integer projectId, String branchName, Integer userId);

    MergeRequestDO acceptMergeRequest(Integer projectId, Integer mergeRequestId, String message, Integer userId);

    Optional<TagNodeDO> getMaxTagNode(Long applicationId, Integer userId);

    void createTag(Integer projectId, String tag, Integer userId);

    TagsDO getTags(Long serviceId, String path, Integer page, Integer size, Integer userId);

    List<TagDO> getTagList(Long appId, Integer userId);

    List<TagDO> getGitLabTags(Integer projectId, Integer userId);
}
