package io.choerodon.devops.domain.service;

/**
 * Created with IntelliJ IDEA.
 * User: Runge
 * Date: 2018/4/9
 * Time: 15:20
 * Description:
 */
public interface IGitFlowService {

    Integer getBranchState(Integer projectId, String branchName, Integer userId);

    String getBranchStatus(String branchName, Integer status);

    String getReleaseNumber(Long serviceId, Integer userId);

    String getHotfixNumber(Long serviceId, Integer userId);

    void createBranch(Integer projectId, String branchName);

    void finishBranch(Integer projectId, String branchName, Boolean isTargetDev, Integer mergeStatus, Integer userId);

    String getTag(Long serviceId, String branchName, Integer userId);

    void createTag(Long serviceId, Integer projectId, String branchName, Integer userId);

    void deleteBranchSafely(
            Integer projectId, String branchName, Integer devMergeStatus, Integer masterMergeStatus, Integer userId);

    void createMergeRequest(Integer projectId, String branchName, Integer userId);
}
