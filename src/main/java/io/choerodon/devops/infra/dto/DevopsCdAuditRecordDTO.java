package io.choerodon.devops.infra.dto;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import io.choerodon.mybatis.annotation.ModifyAudit;
import io.choerodon.mybatis.annotation.VersionAudit;
import io.choerodon.mybatis.domain.AuditDomain;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  19:41 2019/4/3
 * Description:
 */
@ModifyAudit
@VersionAudit
@Table(name = "devops_cd_audit_record")
public class DevopsCdAuditRecordDTO extends AuditDomain {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private Long userId;
    private Long pipelineRecordId;
    private Long stageRecordId;
    private Long jobRecordId;
    private String status;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getPipelineRecordId() {
        return pipelineRecordId;
    }

    public void setPipelineRecordId(Long pipelineRecordId) {
        this.pipelineRecordId = pipelineRecordId;
    }

    public Long getStageRecordId() {
        return stageRecordId;
    }

    public void setStageRecordId(Long stageRecordId) {
        this.stageRecordId = stageRecordId;
    }

    public Long getJobRecordId() {
        return jobRecordId;
    }

    public void setJobRecordId(Long jobRecordId) {
        this.jobRecordId = jobRecordId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "DevopsCdAuditRecordDTO{" +
                "id=" + id +
                ", userId=" + userId +
                ", pipelineRecordId=" + pipelineRecordId +
                ", stageRecordId=" + stageRecordId +
                ", jobRecordId=" + jobRecordId +
                ", status='" + status + '\'' +
                '}';
    }
}
