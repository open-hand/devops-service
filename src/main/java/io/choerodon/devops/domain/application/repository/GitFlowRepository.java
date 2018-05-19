package io.choerodon.devops.domain.application.repository;

import java.util.List;
import java.util.Optional;

import io.choerodon.devops.infra.dataobject.DevopsMergeRequestDO;
import io.choerodon.devops.infra.dataobject.gitlab.BranchDO;
import io.choerodon.devops.infra.dataobject.gitlab.MergeRequestDO;
import io.choerodon.devops.infra.dataobject.gitlab.TagNodeDO;
import io.choerodon.devops.infra.dataobject.gitlab.TagsDO;

/**
 * Created with IntelliJ IDEA.
 * User: Runge
 * Date: 2018/4/9
 * Time: 14:43
 * Description:
 */
public interface GitFlowRepository {
    Integer getGitLabId(Long applicationId);

    void createMergeRequest(Integer projectId, String sourceBranch, String targetBranch, String username);

    MergeRequestDO getMergeRequest(Integer projectId, Long mergeRequestId, String username);

    void deleteMergeRequest(Integer projectId, Integer mergeRequestId);

    DevopsMergeRequestDO getDevOpsMergeRequest(Integer projectId, String branchName, String targetBranch);

    void deleteDevOpsMergeRequest(Long id);

    void updateMergeRequest(Integer projectId, Long mergeRequestId, String username);

    Boolean checkMergeRequestCommit(Integer projectId, Long mergeRequestId);

    void createBranch(Integer projectId, String branchName, String baseBranch);

    List<BranchDO> listBranches(Integer projectId, String path);

    void deleteBranch(Integer projectId, String branchName, String username);

    MergeRequestDO acceptMergeRequest(Integer projectId, Integer mergeRequestId, String message, String username);

    Optional<TagNodeDO> getMaxTagNode(Long applicationId, String username);

    void createTag(Integer projectId, String tag, String username);

    TagsDO getTags(Long serviceId, String path, Integer page, Integer size);
}
