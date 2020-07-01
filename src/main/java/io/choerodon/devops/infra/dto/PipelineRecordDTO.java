package io.choerodon.devops.infra.dto;

import io.choerodon.mybatis.annotation.ModifyAudit;
import io.choerodon.mybatis.annotation.VersionAudit;
import io.choerodon.mybatis.domain.AuditDomain;

import javax.persistence.*;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  19:23 2019/4/3
 * Description:
 */
@ModifyAudit
@VersionAudit
@Table(name = "devops_pipeline_record")
public class PipelineRecordDTO extends AuditDomain {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private Long pipelineId;
    private String status;
    private String triggerType;
    private String bpmDefinition;
    private Long projectId;
    private String pipelineName;
    private String businessKey;
    private Boolean edited;
    private String auditUser;
    private String errorInfo;

    @Transient
    private String stageName;

    @Transient
    private Long stageRecordId;

    @Transient
    private Long taskRecordId;

    @Transient
    private String env;

    @Transient
    private String recordAudit;

    @Transient
    private String stageAudit;

    @Transient
    private String taskAudit;

    public PipelineRecordDTO(Long pipelineId, String triggerType, Long projectId, String status, String pipelineName) {
        this.pipelineId = pipelineId;
        this.triggerType = triggerType;
        this.projectId = projectId;
        this.status = status;
        this.pipelineName = pipelineName;
    }

    public PipelineRecordDTO() {
    }


    public String getErrorInfo() {
        return errorInfo;
    }

    public void setErrorInfo(String errorInfo) {
        this.errorInfo = errorInfo;
    }

    public String getAuditUser() {
        return auditUser;
    }

    public void setAuditUser(String auditUser) {
        this.auditUser = auditUser;
    }

    public Boolean getEdited() {
        return edited;
    }

    public void setEdited(Boolean edited) {
        this.edited = edited;
    }

    public String getBusinessKey() {
        return businessKey;
    }

    public void setBusinessKey(String businessKey) {
        this.businessKey = businessKey;
    }

    public String getPipelineName() {
        return pipelineName;
    }

    public void setPipelineName(String pipelineName) {
        this.pipelineName = pipelineName;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

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

    public String getBpmDefinition() {
        return bpmDefinition;
    }

    public void setBpmDefinition(String bpmDefinition) {
        this.bpmDefinition = bpmDefinition;
    }

    public String getEnv() {
        return env;
    }

    public void setEnv(String env) {
        this.env = env;
    }

    public String getStageName() {
        return stageName;
    }

    public PipelineRecordDTO setStageName(String stageName) {
        this.stageName = stageName;
        return this;
    }

    public Long getStageRecordId() {
        return stageRecordId;
    }

    public void setStageRecordId(Long stageRecordId) {
        this.stageRecordId = stageRecordId;
    }

    public Long getTaskRecordId() {
        return taskRecordId;
    }

    public void setTaskRecordId(Long taskRecordId) {
        this.taskRecordId = taskRecordId;
    }

    public String getRecordAudit() {
        return recordAudit;
    }

    public void setRecordAudit(String recordAudit) {
        this.recordAudit = recordAudit;
    }

    public String getStageAudit() {
        return stageAudit;
    }

    public void setStageAudit(String stageAudit) {
        this.stageAudit = stageAudit;
    }

    public String getTaskAudit() {
        return taskAudit;
    }

    public void setTaskAudit(String taskAudit) {
        this.taskAudit = taskAudit;
    }

}
