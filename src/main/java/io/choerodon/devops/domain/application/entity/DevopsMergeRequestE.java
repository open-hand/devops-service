package io.choerodon.devops.domain.application.entity;

import java.util.Date;

public class DevopsMergeRequestE {
    private Long id;

    private Long projectId;

    private Long gitlabMergeRequestId;

    private Long authorId;

    private Long assigneeId;

    private String sourceBranch;

    private String targetBranch;

    private String state;

    private String title;

    private Date createdAt;

    private Date updatedAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public Long getGitlabMergeRequestId() {
        return gitlabMergeRequestId;
    }

    public void setGitlabMergeRequestId(Long gitlabMergeRequestId) {
        this.gitlabMergeRequestId = gitlabMergeRequestId;
    }

    public Long getAuthorId() {
        return authorId;
    }

    public void setAuthorId(Long authorId) {
        this.authorId = authorId;
    }

    public Long getAssigneeId() {
        return assigneeId;
    }

    public void setAssigneeId(Long assigneeId) {
        this.assigneeId = assigneeId;
    }

    public String getSourceBranch() {
        return sourceBranch;
    }

    public void setSourceBranch(String sourceBranch) {
        this.sourceBranch = sourceBranch;
    }

    public String getTargetBranch() {
        return targetBranch;
    }

    public void setTargetBranch(String targetBranch) {
        this.targetBranch = targetBranch;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public String toString() {
        return "DevopsMergeRequestE{"
                + "id=" + id
                + ", projectId=" + projectId
                + ", gitlabMergeRequestId=" + gitlabMergeRequestId
                + ", authorId=" + authorId
                + ", assigneeId=" + assigneeId
                + ", sourceBranch='" + sourceBranch
                + '\'' + ", targetBranch='" + targetBranch
                + '\'' + ", state='" + state
                + '\'' + ", title='" + title
                + '\'' + ", createdAt=" + createdAt
                + ", updatedAt=" + updatedAt
                + '}';
    }
}
