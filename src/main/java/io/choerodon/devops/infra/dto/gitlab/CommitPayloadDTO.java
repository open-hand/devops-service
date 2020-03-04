package io.choerodon.devops.infra.dto.gitlab;

import java.util.List;

import io.swagger.annotations.ApiModelProperty;

public class CommitPayloadDTO {
    @ApiModelProperty("分支名 / 必要")
    private String branch;
    @ApiModelProperty("提交信息 / 必要")
    private String commitMessage;
    @ApiModelProperty("新开始的新分支从哪里切出 / 可选")
    private String startBranch;
    @ApiModelProperty("新分支起始的SHA值 / 可选")
    private String startSha;
    @ApiModelProperty("用于开始新分支的项目的项目ID或url编码的路径。默认为id的值。/ 可选")
    private Object startProject;
    @ApiModelProperty("文件的操作 / 必要")
    private List<CommitActionDTO> actions;
    @ApiModelProperty("指定提交者的email / 可选")
    private String authorEmail;
    @ApiModelProperty("指定提交者的名称 / 可选")
    private String authorName;
    @ApiModelProperty("包括提交数据。默认是true / 可选")
    private Boolean stats;
    @ApiModelProperty("当true时，基于start_branch或start_sha覆盖目标分支 / 可选")
    private Boolean force;

    public CommitPayloadDTO() {
    }


    public CommitPayloadDTO(String branch, String commitMessage, List<CommitActionDTO> actions) {
        this.branch = branch;
        this.commitMessage = commitMessage;
        this.actions = actions;
    }

    public String getBranch() {
        return branch;
    }

    public void setBranch(String branch) {
        this.branch = branch;
    }

    public String getCommitMessage() {
        return commitMessage;
    }

    public void setCommitMessage(String commitMessage) {
        this.commitMessage = commitMessage;
    }

    public String getStartBranch() {
        return startBranch;
    }

    public void setStartBranch(String startBranch) {
        this.startBranch = startBranch;
    }

    public String getStartSha() {
        return startSha;
    }

    public void setStartSha(String startSha) {
        this.startSha = startSha;
    }

    public Object getStartProject() {
        return startProject;
    }

    public void setStartProject(Object startProject) {
        this.startProject = startProject;
    }

    public List<CommitActionDTO> getActions() {
        return actions;
    }

    public void setActions(List<CommitActionDTO> actions) {
        this.actions = actions;
    }

    public String getAuthorEmail() {
        return authorEmail;
    }

    public void setAuthorEmail(String authorEmail) {
        this.authorEmail = authorEmail;
    }

    public String getAuthorName() {
        return authorName;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    public Boolean getStats() {
        return stats;
    }

    public void setStats(Boolean stats) {
        this.stats = stats;
    }

    public Boolean getForce() {
        return force;
    }

    public void setForce(Boolean force) {
        this.force = force;
    }
}
