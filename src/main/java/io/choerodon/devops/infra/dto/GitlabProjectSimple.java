package io.choerodon.devops.infra.dto;

import io.swagger.annotations.ApiModelProperty;

/**
 * @author scp
 * @date 2020/6/18
 * @description
 */
public class GitlabProjectSimple {
    @ApiModelProperty("项目id")
    private Long projectId;
    @ApiModelProperty("gitlab group id")
    private Long gitlabGroupId;

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public Long getGitlabGroupId() {
        return gitlabGroupId;
    }

    public void setGitlabGroupId(Long gitlabGroupId) {
        this.gitlabGroupId = gitlabGroupId;
    }
}
