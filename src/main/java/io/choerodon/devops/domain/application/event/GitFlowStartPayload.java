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
    private String username;

    public GitFlowStartPayload() {
    }

    /**
     * GitFlow 启动事件
     *
     * @param projectId  GitLab 项目ID
     * @param branchName 分支名称
     * @param username   用户名
     */
    public GitFlowStartPayload(Integer projectId, String branchName, String username) {
        this.projectId = projectId;
        this.branchName = branchName;
        this.username = username;
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

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
