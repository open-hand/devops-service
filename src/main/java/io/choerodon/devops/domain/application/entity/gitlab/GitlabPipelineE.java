package io.choerodon.devops.domain.application.entity.gitlab;

import java.util.Date;

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
    private Date createdAt;
    private Date started_at;
    private Date finished_at;

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

    public Date getStarted_at() {
        return started_at;
    }

    public void setStarted_at(Date started_at) {
        this.started_at = started_at;
    }

    public Date getFinished_at() {
        return finished_at;
    }

    public void setFinished_at(Date finished_at) {
        this.finished_at = finished_at;
    }

    public void initUser(Integer id, String username) {
        this.user = new GitlabUserE(id, username);
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }
}

