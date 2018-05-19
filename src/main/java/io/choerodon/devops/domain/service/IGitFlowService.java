package io.choerodon.devops.domain.service;

/**
 * Created with IntelliJ IDEA.
 * User: Runge
 * Date: 2018/4/9
 * Time: 15:20
 * Description:
 */
public interface IGitFlowService {

    Integer getBranchState(Integer projectId, String branchName, String username);

    String getBranchStatus(String branchName, Integer status);

    String getReleaseNumber(Long serviceId, String username);

    String getHotfixNumber(Long serviceId, String username);

    void createBranch(Integer projectId, String branchName);

    void finishBranch(Integer projectId, String branchName, Boolean isTargetDev, Integer mergeStatus, String username);

    void createTag(Long serviceId, Integer projectId, String branchName, String username);

    void deleteBranchSafely(
            Integer projectId, String branchName, Integer devMergeStatus, Integer masterMergeStatus, String username);

    void createMergeRequest(Integer projectId, String branchName, String username);
}
