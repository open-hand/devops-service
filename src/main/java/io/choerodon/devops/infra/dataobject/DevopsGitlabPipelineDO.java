package io.choerodon.devops.infra.dataobject;

import java.util.Date;
import javax.persistence.*;

import io.choerodon.mybatis.entity.BaseDTO;


@Table(name = "devops_gitlab_pipeline")
public class DevopsGitlabPipelineDO extends BaseDTO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long appId;
    private Long pipelineId;
    private Long pipelineCreateUserId;
    private Long commitId;
    private String stage;
    private String status;
    private Date pipelineCreationDate;

    @Transient
    private String ref;
    @Transient
    private String sha;
    @Transient
    private String content;
    @Transient
    private Long commitUserId;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public Long getAppId() {
        return appId;
    }

    public void setAppId(Long appId) {
        this.appId = appId;
    }

    public Long getCommitId() {
        return commitId;
    }

    public void setCommitId(Long commitId) {
        this.commitId = commitId;
    }

    public String getStage() {
        return stage;
    }

    public void setStage(String stage) {
        this.stage = stage;
    }

    public Date getPipelineCreationDate() {
        return pipelineCreationDate;
    }

    public void setPipelineCreationDate(Date pipelineCreationDate) {
        this.pipelineCreationDate = pipelineCreationDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
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

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Long getCommitUserId() {
        return commitUserId;
    }

    public void setCommitUserId(Long commitUserId) {
        this.commitUserId = commitUserId;
    }

}
