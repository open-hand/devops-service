package io.choerodon.devops.domain.application.entity.gitlab;

import java.util.Date;

import org.codehaus.jackson.annotate.JsonProperty;

import io.choerodon.devops.infra.common.util.enums.PipelineStatus;

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
    @JsonProperty(value = "started_at")
    private Date startedAt;
    @JsonProperty(value = "finished_at")
    private Date finishedAt;

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
}

