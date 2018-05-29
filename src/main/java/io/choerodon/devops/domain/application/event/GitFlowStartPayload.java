package io.choerodon.devops.domain.application.event;

/**
 * Creator: Runge
 * Date: 2018/4/12
 * Time: 15:47
 * Description:
 */
public class GitFlowStartPayload {
    private Integer projectId;
    private String branchName;
    private Integer userId;

    public GitFlowStartPayload() {
    }

    /**
     * GitFlow 启动事件
     *
     * @param projectId  GitLab 项目ID
     * @param branchName 分支名称
     * @param userId     用户Id
     */
    public GitFlowStartPayload(Integer projectId, String branchName, Integer userId) {
        this.projectId = projectId;
        this.branchName = branchName;
        this.userId = userId;
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

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }
}
