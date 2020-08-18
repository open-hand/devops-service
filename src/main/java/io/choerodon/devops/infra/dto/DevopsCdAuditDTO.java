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
 * Date:  19:43 2019/4/3
 * Description:
 */
@ModifyAudit
@VersionAudit
@Table(name = "devops_cd_audit")
public class DevopsCdAuditDTO extends AuditDomain {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private Long userId;
    private Long projectId;
    private Long pipelineId;
    private Long cdStageId;
    private Long cdJobId;

    public DevopsCdAuditDTO() {

    }

    public DevopsCdAuditDTO(Long pipelineId, Long cdStageId, Long cdJobId) {
        this.pipelineId = pipelineId;
        this.cdStageId = cdStageId;
        this.cdJobId = cdJobId;
    }

    public DevopsCdAuditDTO(Long projectId, Long pipelineId, Long cdStageId, Long cdJobId) {
        this.projectId = projectId;
        this.pipelineId = pipelineId;
        this.cdStageId = cdStageId;
        this.cdJobId = cdJobId;
    }

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

    public Long getPipelineId() {
        return pipelineId;
    }

    public void setPipelineId(Long pipelineId) {
        this.pipelineId = pipelineId;
    }

    public Long getCdStageId() {
        return cdStageId;
    }

    public void setCdStageId(Long cdStageId) {
        this.cdStageId = cdStageId;
    }

    public Long getCdJobId() {
        return cdJobId;
    }

    public void setCdJobId(Long cdJobId) {
        this.cdJobId = cdJobId;
    }

    public Long getProjectId() {
        return projectId;
    }

    public DevopsCdAuditDTO setProjectId(Long projectId) {
        this.projectId = projectId;
        return this;
    }

    @Override
    public String toString() {
        return "DevopsCdAuditDTO{" +
                "id=" + id +
                ", userId=" + userId +
                ", projectId=" + projectId +
                ", pipelineId=" + pipelineId +
                ", cdStageId=" + cdStageId +
                ", cdJobId=" + cdJobId +
                '}';
    }
}
