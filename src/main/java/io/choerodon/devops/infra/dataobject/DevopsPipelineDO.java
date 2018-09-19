package io.choerodon.devops.infra.dataobject;

import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

import io.choerodon.mybatis.annotation.ModifyAudit;
import io.choerodon.mybatis.annotation.VersionAudit;


@VersionAudit
@ModifyAudit
@Table(name = "devops_gitlab_pipeline")
public class DevopsPipelineDO {

    @Id
    @GeneratedValue
    private Long id;
    private Long pipelineId;
    private Long pipelineCreateUserId;
    private Long commitUserId;
    private String commmitContent;
    private String sha;
    private String ref;
    private String stage;
    private String status;
    private Date pipelineCreationDate;


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

    public Long getCommitUserId() {
        return commitUserId;
    }

    public void setCommitUserId(Long commitUserId) {
        this.commitUserId = commitUserId;
    }

    public String getCommmitContent() {
        return commmitContent;
    }

    public void setCommmitContent(String commmitContent) {
        this.commmitContent = commmitContent;
    }

    public String getSha() {
        return sha;
    }

    public void setSha(String sha) {
        this.sha = sha;
    }

    public String getRef() {
        return ref;
    }

    public void setRef(String ref) {
        this.ref = ref;
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
}
