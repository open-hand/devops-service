package io.choerodon.devops.domain.application.event;

/**
 * Created by younger on 2018/4/16.
 */
public class KanbanProjectPayload {

    private Long projectId;
    private Integer gitlabGroupId;
    private Integer gitlabProjectId;
    private String projectName;

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public Integer getGitlabGroupId() {
        return gitlabGroupId;
    }

    public void setGitlabGroupId(Integer gitlabGroupId) {
        this.gitlabGroupId = gitlabGroupId;
    }

    public Integer getGitlabProjectId() {
        return gitlabProjectId;
    }

    public void setGitlabProjectId(Integer gitlabProjectId) {
        this.gitlabProjectId = gitlabProjectId;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }
}
