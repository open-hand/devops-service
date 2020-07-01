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
    private Long cicdPipelineId;
    private Long cicdStageId;
    private Long cicdJobId;

    public DevopsCdAuditDTO() {

    }

    public DevopsCdAuditDTO(Long cicdPipelineId, Long cicdStageId, Long cicdJobId) {
        this.cicdPipelineId = cicdPipelineId;
        this.cicdStageId = cicdStageId;
        this.cicdJobId = cicdJobId;
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

    public Long getCicdPipelineId() {
        return cicdPipelineId;
    }

    public void setCicdPipelineId(Long cicdPipelineId) {
        this.cicdPipelineId = cicdPipelineId;
    }

    public Long getCicdStageId() {
        return cicdStageId;
    }

    public void setCicdStageId(Long cicdStageId) {
        this.cicdStageId = cicdStageId;
    }

    public Long getCicdJobId() {
        return cicdJobId;
    }

    public void setCicdJobId(Long cicdJobId) {
        this.cicdJobId = cicdJobId;
    }

    @Override
    public String toString() {
        return "DevopsCdAuditDTO{" +
                "id=" + id +
                ", userId=" + userId +
                ", cicdPipelineId=" + cicdPipelineId +
                ", cicdStageId=" + cicdStageId +
                ", cicdJobId=" + cicdJobId +
                '}';
    }
}
