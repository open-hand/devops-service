package io.choerodon.devops.infra.dto;

import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import io.choerodon.mybatis.annotation.ModifyAudit;
import io.choerodon.mybatis.annotation.VersionAudit;
import io.choerodon.mybatis.domain.AuditDomain;

/**
 * ci 人工卡点用户审核记录表(CiAuditUserRecord)实体类
 *
 * @author hao.wang@zknow.com
 * @since 2022-11-03 10:32:19
 */

@ApiModel("ci 人工卡点用户审核记录表")
@VersionAudit
@ModifyAudit
@JsonInclude(value = JsonInclude.Include.NON_NULL)
@Table(name = "devops_ci_audit_user_record")
public class CiAuditUserRecordDTO extends AuditDomain {
    public static final String FIELD_ID = "id";
    public static final String FIELD_AUDIT_RECORD_ID = "auditRecordId";
    public static final String FIELD_USER_ID = "userId";
    public static final String FIELD_STATUS = "status";
    private static final long serialVersionUID = -64986160292709353L;
    @Id
    @GeneratedValue
    private Long id;
    @ApiModelProperty(value = "关联devops流水线id")
    private Long ciPipelineId;
    @ApiModelProperty(value = "devops_ci_audit_record.id", required = true)
    @NotNull
    private Long auditRecordId;

    @ApiModelProperty(value = "用户Id", required = true)
    @NotNull
    private Long userId;

    @ApiModelProperty(value = "人工审核的结果（待审核、拒绝、通过）", required = true)
    @NotBlank
    private String status;

    public CiAuditUserRecordDTO() {
    }

    public CiAuditUserRecordDTO(Long ciPipelineId, @NotNull Long auditRecordId, @NotNull Long userId, @NotBlank String status) {
        this.ciPipelineId = ciPipelineId;
        this.auditRecordId = auditRecordId;
        this.userId = userId;
        this.status = status;
    }

    public Long getCiPipelineId() {
        return ciPipelineId;
    }

    public void setCiPipelineId(Long ciPipelineId) {
        this.ciPipelineId = ciPipelineId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getAuditRecordId() {
        return auditRecordId;
    }

    public void setAuditRecordId(Long auditRecordId) {
        this.auditRecordId = auditRecordId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

}

