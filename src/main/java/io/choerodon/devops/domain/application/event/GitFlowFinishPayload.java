package io.choerodon.devops.domain.application.event;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: hfr
 * Date: 2018-01-26
 */

public class GitFlowFinishPayload {
    private Long applicationId;
    private Integer projectId;
    private String branchName;
    private Integer devMergeStatus;
    private Integer masterMergeStatus;
    private String username;

    public GitFlowFinishPayload() {
    }

    /**
     * GitFlow 结束事件
     *
     * @param applicationId     应用ID
     * @param projectId         GitLab 项目ID
     * @param branchName        分支名称
     * @param devMergeStatus    对develop分支合并请求状态
     * @param masterMergeStatus 对master分支合并请求状态
     * @param username          用户名
     */
    public GitFlowFinishPayload(Long applicationId,
                                Integer projectId,
                                String branchName,
                                Integer devMergeStatus,
                                Integer masterMergeStatus,
                                String username) {
        this.applicationId = applicationId;
        this.projectId = projectId;
        this.branchName = branchName;
        this.devMergeStatus = devMergeStatus;
        this.masterMergeStatus = masterMergeStatus;
        this.username = username;
    }

    public Long getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(Long applicationId) {
        this.applicationId = applicationId;
    }

    public Integer getProjectId() {
        return projectId;
    }

    public void setProjectId(Integer projectId) {
        this.projectId = projectId;
    }

    public String getBranchName() {
        return branchName;
    }

    public void setBranchName(String branchName) {
        this.branchName = branchName;
    }

    public Integer getDevMergeStatus() {
        return devMergeStatus;
    }

    public void setDevMergeStatus(Integer devMergeStatus) {
        this.devMergeStatus = devMergeStatus;
    }

    public Integer getMasterMergeStatus() {
        return masterMergeStatus;
    }

    public void setMasterMergeStatus(Integer masterMergeStatus) {
        this.masterMergeStatus = masterMergeStatus;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
