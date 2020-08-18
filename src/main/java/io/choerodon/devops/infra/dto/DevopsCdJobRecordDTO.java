package io.choerodon.devops.infra.dto;

import java.util.Date;
import javax.persistence.*;

import io.swagger.annotations.ApiModelProperty;

import io.choerodon.mybatis.annotation.ModifyAudit;
import io.choerodon.mybatis.annotation.VersionAudit;
import io.choerodon.mybatis.domain.AuditDomain;

@ModifyAudit
@VersionAudit
@Table(name = "devops_cd_job_record")
public class DevopsCdJobRecordDTO extends AuditDomain {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String name;
    private Long jobId;
    private Long stageRecordId;
    private String type;
    private String status;
    private String triggerType;
    private String triggerValue;
    private Long projectId;
    private String metadata;
    @ApiModelProperty("是否会签")
    private Integer countersigned;

    @ApiModelProperty("任务顺序")
    private Long sequence;

    private Date startedDate;
    private Date finishedDate;
    private Long durationSeconds;

    @ApiModelProperty("主机部署 制品库详情")
    private String deployMetadata;
    private Long deployInfoId;
    private Long commandId;

    @Transient
    @ApiModelProperty("流水线记录id")
    private Long pipelineRecordId;

    @Transient
    @ApiModelProperty("阶段名称")
    private String stageName;

    @Transient
    @ApiModelProperty("流水线名称")
    private String pipelineName;

    public Long getPipelineRecordId() {
        return pipelineRecordId;
    }

    public DevopsCdJobRecordDTO setPipelineRecordId(Long pipelineRecordId) {
        this.pipelineRecordId = pipelineRecordId;
        return this;
    }

    public String getStageName() {
        return stageName;
    }

    public DevopsCdJobRecordDTO setStageName(String stageName) {
        this.stageName = stageName;
        return this;
    }

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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getStageRecordId() {
        return stageRecordId;
    }

    public void setStageRecordId(Long stageRecordId) {
        this.stageRecordId = stageRecordId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
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

    public String getTriggerValue() {
        return triggerValue;
    }

    public void setTriggerValue(String triggerValue) {
        this.triggerValue = triggerValue;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public String getMetadata() {
        return metadata;
    }

    public void setMetadata(String metadata) {
        this.metadata = metadata;
    }

    public Integer getCountersigned() {
        return countersigned;
    }

    public void setCountersigned(Integer countersigned) {
        this.countersigned = countersigned;
    }

    public Long getSequence() {
        return sequence;
    }

    public void setSequence(Long sequence) {
        this.sequence = sequence;
    }

    public Date getStartedDate() {
        return startedDate;
    }

    public void setStartedDate(Date startedDate) {
        this.startedDate = startedDate;
    }

    public Date getFinishedDate() {
        return finishedDate;
    }

    public void setFinishedDate(Date finishedDate) {
        this.finishedDate = finishedDate;
    }

    public String getDeployMetadata() {
        return deployMetadata;
    }

    public void setDeployMetadata(String deployMetadata) {
        this.deployMetadata = deployMetadata;
    }

    public Long getDurationSeconds() {
        return durationSeconds;
    }

    public void setDurationSeconds(Long durationSeconds) {
        this.durationSeconds = durationSeconds;
    }

    public Long getJobId() {
        return jobId;
    }

    public void setJobId(Long jobId) {
        this.jobId = jobId;
    }

    public Long getDeployInfoId() {
        return deployInfoId;
    }

    public void setDeployInfoId(Long deployInfoId) {
        this.deployInfoId = deployInfoId;
    }

    public Long getCommandId() {
        return commandId;
    }

    public void setCommandId(Long commandId) {
        this.commandId = commandId;
    }

    @Override
    public String toString() {
        return "DevopsCdJobRecordDTO{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", jobId=" + jobId +
                ", stageRecordId=" + stageRecordId +
                ", type='" + type + '\'' +
                ", status='" + status + '\'' +
                ", triggerType='" + triggerType + '\'' +
                ", triggerValue='" + triggerValue + '\'' +
                ", projectId=" + projectId +
                ", metadata='" + metadata + '\'' +
                ", countersigned=" + countersigned +
                ", sequence=" + sequence +
                ", startedDate=" + startedDate +
                ", finishedDate=" + finishedDate +
                ", durationSeconds=" + durationSeconds +
                ", deployMetadata='" + deployMetadata + '\'' +
                ", deployInfoId=" + deployInfoId +
                ", commandId=" + commandId +
                ", pipelineRecordId=" + pipelineRecordId +
                ", stageName='" + stageName + '\'' +
                ", pipelineName='" + pipelineName + '\'' +
                "} ";
    }
}
