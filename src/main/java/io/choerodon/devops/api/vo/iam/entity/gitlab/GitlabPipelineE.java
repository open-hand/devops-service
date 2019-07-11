package io.choerodon.devops.api.vo.iam.entity.gitlab;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.choerodon.devops.infra.enums.PipelineStatus;

/**
 * Created by Zenger on 2018/4/3.
 */
public class GitlabPipelineE {

    private Integer id;
    private PipelineStatus status;
    private String ref;
    private String sha;
    private GitlabUserE user;

    @JsonProperty(value = "created_at")
    private String createdAt;
    @JsonProperty(value = "updated_at")
    private Date updatedAt;
    @JsonProperty(value = "started_at")
    private Date startedAt;
    @JsonProperty(value = "finished_at")
    private Date finishedAt;
    @JsonProperty(value = "committed_at")
    private Date committedAt;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public PipelineStatus getStatus() {
        return status;
    }

    public void setStatus(PipelineStatus status) {
        this.status = status;
    }

    public String getRef() {
        return ref;
    }

    public void setRef(String ref) {
        this.ref = ref;
    }

    public String getSha() {
        return sha;
    }

    public void setSha(String sha) {
        this.sha = sha;
    }

    public GitlabUserE getUser() {
        return user;
    }

    public void setUser(GitlabUserE user) {
        this.user = user;
    }


    public void initUser(Integer id, String username) {
        this.user = new GitlabUserE(id, username);
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Date getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(Date startedAt) {
        this.startedAt = startedAt;
    }

    public Date getFinishedAt() {
        return finishedAt;
    }

    public void setFinishedAt(Date finishedAt) {
        this.finishedAt = finishedAt;
    }

    public Date getCommittedAt() {
        return committedAt;
    }

    public void setCommittedAt(Date committedAt) {
        this.committedAt = committedAt;
    }
}

