package io.choerodon.devops.infra.dto;

import javax.persistence.*;

import io.swagger.annotations.ApiModelProperty;

import io.choerodon.mybatis.annotation.ModifyAudit;
import io.choerodon.mybatis.annotation.VersionAudit;
import io.choerodon.mybatis.domain.AuditDomain;

@ModifyAudit
@VersionAudit
@Table(name = "devops_cd_stage_record")
public class DevopsCdStageRecordDTO extends AuditDomain {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private Long pipelineRecordId;
    private Long sequence;
    private String status;
    private String triggerType;
    private Long projectId;
    private Long stageId;
    private String stageName;

    @Transient
    @ApiModelProperty("流水线名称")
    private String pipelineName;

    @Transient
    @ApiModelProperty("流水线id")
    private Long pipelineId;

    @Transient
    @ApiModelProperty("ci 和 cd 关联关系id")
    private Long devopsPipelineRecordRelId;

    public String getPipelineName() {
        return pipelineName;
    }

    public void setPipelineName(String pipelineName) {
        this.pipelineName = pipelineName;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getPipelineRecordId() {
        return pipelineRecordId;
    }

    public void setPipelineRecordId(Long pipelineRecordId) {
        this.pipelineRecordId = pipelineRecordId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getTriggerType() {
        return triggerType;
    }

    public void setTriggerType(String triggerType) {
        this.triggerType = triggerType;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public Long getStageId() {
        return stageId;
    }

    public void setStageId(Long stageId) {
        this.stageId = stageId;
    }

    public String getStageName() {
        return stageName;
    }

    public void setStageName(String stageName) {
        this.stageName = stageName;
    }


    public Long getSequence() {
        return sequence;
    }

    public void setSequence(Long sequence) {
        this.sequence = sequence;
    }

    public Long getPipelineId() {
        return pipelineId;
    }

    public DevopsCdStageRecordDTO setPipelineId(Long pipelineId) {
        this.pipelineId = pipelineId;
        return this;
    }

    public Long getDevopsPipelineRecordRelId() {
        return devopsPipelineRecordRelId;
    }

    public DevopsCdStageRecordDTO setDevopsPipelineRecordRelId(Long devopsPipelineRecordRelId) {
        this.devopsPipelineRecordRelId = devopsPipelineRecordRelId;
        return this;
    }

    @Override
    public String toString() {
        return "DevopsCdStageRecordDTO{" +
                "id=" + id +
                ", pipelineRecordId=" + pipelineRecordId +
                ", sequence=" + sequence +
                ", status='" + status + '\'' +
                ", triggerType='" + triggerType + '\'' +
                ", projectId=" + projectId +
                ", stageId=" + stageId +
                ", stageName='" + stageName + '\'' +
                ", pipelineName='" + pipelineName + '\'' +
                ", pipelineId=" + pipelineId +
                ", devopsPipelineRecordRelId=" + devopsPipelineRecordRelId +
                "} ";
    }
}
