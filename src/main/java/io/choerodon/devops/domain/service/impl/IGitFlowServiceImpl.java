package io.choerodon.devops.domain.service.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.domain.application.entity.UserAttrE;
import io.choerodon.devops.domain.application.repository.GitFlowRepository;
import io.choerodon.devops.domain.application.repository.UserAttrRepository;
import io.choerodon.devops.domain.service.IGitFlowService;
import io.choerodon.devops.infra.common.util.GitUserNameUtil;
import io.choerodon.devops.infra.common.util.TypeUtil;
import io.choerodon.devops.infra.dataobject.DevopsMergeRequestDO;
import io.choerodon.devops.infra.dataobject.gitlab.MergeRequestDO;
import io.choerodon.devops.infra.dataobject.gitlab.TagDO;
import io.choerodon.devops.infra.dataobject.gitlab.TagNodeDO;

/**
 * Created with IntelliJ IDEA.
 * User: Runge
 * Date: 2018/4/9
 * Time: 15:21
 * Description:
 */
@Component
public class IGitFlowServiceImpl implements IGitFlowService {

    private static final String BRANCH_MASTER = "master";
    private static final String BRANCH_DEV = "develop";
    private static final String FEATURE_PREFIX = "feature-";
    private static final String RELEASE_PREFIX = "release-";
    private static final String HOTFIX_PREFIX = "hotfix-";
    private static final String CAN_BE_MERGED = "can_be_merged";
    private static final String MERGED = "merged";

    private String[] featureBranchStatus = {
            "can_be_merge", "dev_conflict", "no_commit", MERGED
    };
    private String[] branchStatus = {
            "can_be_merge", "dev_conflict_and_master_can_merge",
            "dev_no_commit_and_master_can_merge", "dev_merged_and_master_can_merge",
            "dev_can_merge_and_master_conflict", "both_conflict",
            "dev_no_commit_and_master_conflict", "dev_merged_and_master_conflict",
            "dev_can_merge_and_master_no_commit", "dev_conflict_and_master_no_commit",
            "both_no_commit", "dev_merged_and_master_no_commit",
            "dev_can_merge_and_master_merged", "dev_conflict_and_master_merged",
            "dev_no_commit_and_master_merged", "both_merged"
    };

    @Autowired
    private GitFlowRepository gitFlowRepository;
    @Autowired
    private UserAttrRepository userAttrRepository;


    public Integer getGitlabUserId() {
        UserAttrE userAttrE = userAttrRepository.queryById(TypeUtil.objToLong(GitUserNameUtil.getUserId()));
        return TypeUtil.objToInteger(userAttrE.getGitlabUserId());
    }

    private String mergeRequestMessage(String sourceBranch, String targetBranch) {
        return "Merge '" + sourceBranch + "' into '" + targetBranch + "'.";
    }

    /**
     * +---+-----------------------------------------------------------------+
     * |   |                      d e v e l o p                              |
     * +------------------+--------------+--------------+-----------+--------+
     * |   |              | can_be_merge | has_conflict | no_commit | merged |
     * |   +-----------------------------------------------------------------+
     * | m | can_be_merge |   0          |   1          |   2       |   3    |
     * | a +-----------------------------------------------------------------+
     * | s | has_conflict |   4          |   5          |   6       |   7    |
     * | t +-----------------------------------------------------------------+
     * | e | no_commit    |   8          |   9          |   10      |   11   |
     * | r +-----------------------------------------------------------------+
     * |   | merged       |   12         |   13         |   14      |   15   |
     * +---+--------------+--------------+--------------+-----------+--------+
     *
     * @param projectId  工程ID
     * @param branchName 分支名称
     * @return mergeStatus 分支状态
     */
    public Integer getBranchState(Integer projectId, String branchName, Integer userId) {
        Integer outcome = 0; // 看上面的备注表格
        outcome += calculateMergeStatus(projectId, branchName, userId, true);
        if (!branchName.startsWith(FEATURE_PREFIX)) {
            outcome += calculateMergeStatus(projectId, branchName, userId, false);
        }
        return outcome;
    }

    @Override
    public String getBranchStatus(String branchName, Integer status) {
        return branchName.startsWith(FEATURE_PREFIX) ? featureBranchStatus[status] : branchStatus[status];
    }

    @Override
    public String getReleaseNumber(Long serviceId, Integer userId) {
        return getBranchNumber(serviceId, userId, true);
    }

    @Override
    public String getHotfixNumber(Long serviceId, Integer userId) {
        return getBranchNumber(serviceId, userId, false);
    }

    @Override
    public void createBranch(Integer projectId, String branchName) {
        Boolean isHotFix = branchName.startsWith(HOTFIX_PREFIX);
        gitFlowRepository.createBranch(projectId, branchName, isHotFix ? BRANCH_MASTER : BRANCH_DEV, getGitlabUserId());
    }

    @Override
    public void finishBranch(Integer projectId, String branchName, Boolean isTargetDev, Integer mergeStatus, Integer userId) {
        if (mergeStatus == 0) {
            String targetBranch = isTargetDev ? BRANCH_DEV : BRANCH_MASTER;
            Integer mergeRequestId = gitFlowRepository
                    .getDevOpsMergeRequest(projectId, branchName, targetBranch)
                    .getMergeRequestId().intValue();
            acceptBranchMR(projectId, mergeRequestId, branchName, targetBranch, userId);
        }
    }

    @Override
    public String getTag(Long serviceId, String branchName, Integer userId) {
        String tag = branchName.split("-")[1];
        List<TagDO> tagList = gitFlowRepository.getTagList(serviceId, userId);
        String branchTag = tag;
        if (!tag.matches("\\d+(\\.\\d+){2}")
                || tagList.parallelStream().anyMatch(t -> branchTag.equals(t.getName()))) {
            if (branchName.startsWith(RELEASE_PREFIX)) {
                tag = getReleaseNumber(serviceId, userId);
            } else if (branchName.startsWith(HOTFIX_PREFIX)) {
                tag = getHotfixNumber(serviceId, userId);
            } else {
                throw new CommonException("create.tag.wrong.branch");
            }
        }
        return tag;
    }

    @Override
    public void createTag(Long serviceId, Integer projectId, String branchName, Integer userId) {
        String tag = getTag(serviceId, branchName, userId);
        gitFlowRepository.createTag(projectId, tag, userId);
    }

    @Override
    public void deleteBranchSafely(Integer projectId, String branchName, Integer devMergeStatus, Integer masterMergeStatus, Integer userId) {
        deleteBranchRecord(projectId, branchName, BRANCH_DEV, devMergeStatus, userId);
        if (!branchName.startsWith(FEATURE_PREFIX)) {
            deleteBranchRecord(projectId, branchName, BRANCH_MASTER, masterMergeStatus, userId);
        }
        gitFlowRepository.deleteBranch(projectId, branchName, userId);
    }

    @Override
    public void createMergeRequest(Integer projectId, String branchName, Integer userId) {
        gitFlowRepository.createMergeRequest(projectId, branchName, BRANCH_DEV, userId);
        if (!branchName.startsWith(FEATURE_PREFIX)) {
            gitFlowRepository.createMergeRequest(projectId, branchName, BRANCH_MASTER, userId);
        }
    }


    /**
     * +---------+--------------+--------------+-----------+--------+
     * |  branch | can_be_merge | has_conflict | no_commit | merged |
     * |         +--------------+--------------+-----------+--------+
     * |         |   0          |   1          |   2       |   3    |
     * +---------+--------------+--------------+-----------+--------+
     *
     * @param applicationId 应用ID
     * @param branchName    分支名称
     * @param userId        用户Id
     * @param isTargetDev   目标分支是否为develop
     * @return 合并状态，master分支结果乘 4 方便状态计算
     */
    private Integer calculateMergeStatus(
            Integer applicationId,
            String branchName,
            Integer userId,
            Boolean isTargetDev
    ) {
        Long mergeRequestId = gitFlowRepository.getDevOpsMergeRequest(
                applicationId, branchName, isTargetDev ? BRANCH_DEV : BRANCH_MASTER).getMergeRequestId();
        gitFlowRepository.updateMergeRequest(applicationId, mergeRequestId, userId);
        MergeRequestDO mergeRequest = gitFlowRepository.getMergeRequest(applicationId, mergeRequestId, userId);
        String mergeRequestState = mergeRequest.getState();
        Integer statusCount = 0;
        if (MERGED.equals(mergeRequestState)) {
            statusCount = 3;
        } else {
            String mergeRequestMergeStatus = mergeRequest.getMergeStatus();
            if (!CAN_BE_MERGED.equals(mergeRequestMergeStatus)) {
                statusCount = gitFlowRepository.checkMergeRequestCommit(applicationId, mergeRequestId, userId) ? 2 : 1;
            }
        }
        return isTargetDev ? statusCount : statusCount * 4;
    }

    private String getBranchNumber(Long applicationId, Integer userId, Boolean isRealease) {
        Optional<TagNodeDO> maxTagNode = gitFlowRepository.getMaxTagNode(applicationId, userId);
        if (maxTagNode.isPresent()) {
            TagNodeDO node = maxTagNode.get();
            return node.getNextTag(isRealease);
        } else {
            return "1.0.0";
        }
    }

    private void acceptBranchMR(
            Integer projectId,
            Integer mergeRequestId,
            String branchName,
            String targetBranch,
            Integer userId
    ) {
        String message = mergeRequestMessage(branchName, targetBranch);
        MergeRequestDO mergeRequest = gitFlowRepository
                .acceptMergeRequest(projectId, mergeRequestId, message, userId);
        if (!MERGED.equals(mergeRequest.getState())) {
            throw new CommonException("error.mergeRequest.accept");
        }
    }

    private void deleteBranchRecord(Integer projectId, String branchName, String targetBranch,
                                    Integer mergeStatus, Integer userId) {
        DevopsMergeRequestDO mergeRequestDO = gitFlowRepository
                .getDevOpsMergeRequest(projectId, branchName, targetBranch);
        Long mergeRequestId = mergeRequestDO.getMergeRequestId();
        gitFlowRepository.deleteDevOpsMergeRequest(mergeRequestDO.getId());
        if (mergeStatus == 2) {
            gitFlowRepository.deleteMergeRequest(projectId, mergeRequestId.intValue(), userId);
        }
    }
}
