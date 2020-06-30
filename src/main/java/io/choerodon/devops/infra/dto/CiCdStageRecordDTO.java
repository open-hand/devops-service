package io.choerodon.devops.infra.dto;

import io.swagger.annotations.ApiModelProperty;
import javax.persistence.*;

import io.choerodon.mybatis.annotation.ModifyAudit;
import io.choerodon.mybatis.annotation.VersionAudit;
import io.choerodon.mybatis.domain.AuditDomain;

@ModifyAudit
@VersionAudit
@Table(name = "cicd_stage_record")
public class CiCdStageRecordDTO extends AuditDomain {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @ApiModelProperty(name = "阶段名称")
    private String name;
    @ApiModelProperty(name = "阶段Id")
    private Long cicdStageId;

    @ApiModelProperty(name = "状态")
    private String status;
    @ApiModelProperty(name = "阶段顺序")
    private Long sequence;
    @ApiModelProperty(name = "流水线id")
    private Long cicdPipelineId;

    @ApiModelProperty(name = "type")
    private String type;
    @ApiModelProperty(name = "流水线记录Id")
    private Long cicdPipelineRecordId;
    @ApiModelProperty(name = "触发方式")
    private String triggerType;
    @ApiModelProperty(name = "是否并行")
    @Column(name = "is_parallel")
    private Boolean parallel;

    @ApiModelProperty(name = "'执行时间")
    private String executionTime;
    @ApiModelProperty(name = "项目Id")
    private Long projectId;
    @ApiModelProperty(name = "审核人员")
    private String auditUser;

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

    public Long getCicdStageId() {
        return cicdStageId;
    }

    public void setCicdStageId(Long cicdStageId) {
        this.cicdStageId = cicdStageId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Long getSequence() {
        return sequence;
    }

    public void setSequence(Long sequence) {
        this.sequence = sequence;
    }

    public Long getCicdPipelineId() {
        return cicdPipelineId;
    }

    public void setCicdPipelineId(Long cicdPipelineId) {
        this.cicdPipelineId = cicdPipelineId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Long getCicdPipelineRecordId() {
        return cicdPipelineRecordId;
    }

    public void setCicdPipelineRecordId(Long cicdPipelineRecordId) {
        this.cicdPipelineRecordId = cicdPipelineRecordId;
    }

    public String getTriggerType() {
        return triggerType;
    }

    public void setTriggerType(String triggerType) {
        this.triggerType = triggerType;
    }

    public Boolean getParallel() {
        return parallel;
    }

    public void setParallel(Boolean parallel) {
        this.parallel = parallel;
    }

    public String getExecutionTime() {
        return executionTime;
    }

    public void setExecutionTime(String executionTime) {
        this.executionTime = executionTime;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public String getAuditUser() {
        return auditUser;
    }

    public void setAuditUser(String auditUser) {
        this.auditUser = auditUser;
    }

    @Override
    public String toString() {
        return "CiCdStageRecordDTO{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", cicdStageId=" + cicdStageId +
                ", status='" + status + '\'' +
                ", sequence=" + sequence +
                ", cicdPipelineId=" + cicdPipelineId +
                ", type='" + type + '\'' +
                ", cicdPipelineRecordId=" + cicdPipelineRecordId +
                ", triggerType='" + triggerType + '\'' +
                ", parallel=" + parallel +
                ", executionTime='" + executionTime + '\'' +
                ", projectId=" + projectId +
                ", auditUser='" + auditUser + '\'' +
                '}';
    }
}
