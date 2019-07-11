package io.choerodon.devops.api.vo.iam.entity;

import java.util.Date;

public class DevopsGitlabPipelineE {

    private Long id;
    private Long appId;
    private Long pipelineId;
    private Long pipelineCreateUserId;
    private DevopsGitlabCommitE devopsGitlabCommitE;
    private String stage;
    private String status;
    private Date pipelineCreationDate;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getAppId() {
        return appId;
    }

    public void setAppId(Long appId) {
        this.appId = appId;
    }

    public Long getPipelineId() {
        return pipelineId;
    }

    public void setPipelineId(Long pipelineId) {
        this.pipelineId = pipelineId;
    }

    public Long getPipelineCreateUserId() {
        return pipelineCreateUserId;
    }

    public void setPipelineCreateUserId(Long pipelineCreateUserId) {
        this.pipelineCreateUserId = pipelineCreateUserId;
    }

    public DevopsGitlabCommitE getDevopsGitlabCommitE() {
        return devopsGitlabCommitE;
    }

    public void initDevopsGitlabCommitE(Long id, String ref, String sha, Long userId, String content) {
        this.devopsGitlabCommitE = new DevopsGitlabCommitE(id, sha, ref, content, userId);
    }

    public void initDevopsGitlabCommitEById(Long id) {
        this.devopsGitlabCommitE = new DevopsGitlabCommitE(id);
    }

    public String getStage() {
        return stage;
    }

    public void setStage(String stage) {
        this.stage = stage;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Date getPipelineCreationDate() {
        return pipelineCreationDate;
    }

    public void setPipelineCreationDate(Date pipelineCreationDate) {
        this.pipelineCreationDate = pipelineCreationDate;
    }
}
