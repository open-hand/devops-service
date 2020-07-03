package io.choerodon.devops.infra.dto;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

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
    private Long stageRecordId;
    private String type;
    private String status;
    private String triggerType;
    private String triggerValue;
    private Long projectId;
    private String metadata;

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

    @Override
    public String toString() {
        return "DevopsCdJobRecordDTO{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", stageRecordId=" + stageRecordId +
                ", type='" + type + '\'' +
                ", status='" + status + '\'' +
                ", triggerType='" + triggerType + '\'' +
                ", triggerValue='" + triggerValue + '\'' +
                ", projectId=" + projectId +
                ", metadata='" + metadata + '\'' +
                '}';
    }
}
