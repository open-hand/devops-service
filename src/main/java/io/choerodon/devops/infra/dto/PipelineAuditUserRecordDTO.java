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
 * 人工卡点用户审核记录表(PipelineAuditUserRecord)实体类
 *
 * @author
 * @since 2022-11-23 16:42:19
 */

@ApiModel("人工卡点用户审核记录表")
@VersionAudit
@ModifyAudit
@JsonInclude(value = JsonInclude.Include.NON_NULL)
@Table(name = "devops_pipeline_audit_user_record")
public class PipelineAuditUserRecordDTO extends AuditDomain {
    public static final String FIELD_ID = "id";
    public static final String FIELD_AUDIT_RECORD_ID = "auditRecordId";
    public static final String FIELD_USER_ID = "userId";
    public static final String FIELD_STATUS = "status";
    private static final long serialVersionUID = 225225704032835819L;
    @Id
    @GeneratedValue
    private Long id;

    @ApiModelProperty(value = "devops_pipeline.id", required = true)
    @NotNull
    private Long pipelineId;

    @ApiModelProperty(value = "devops_pipeline_audit_record.id", required = true)
    @NotNull
    private Long auditRecordId;

    @ApiModelProperty(value = "用户Id", required = true)
    @NotNull
    private Long userId;

    @ApiModelProperty(value = "人工审核的结果（待审核、拒绝、通过）", required = true)
    @NotBlank
    private String status;

    public PipelineAuditUserRecordDTO() {
    }

    public PipelineAuditUserRecordDTO(Long pipelineId, Long auditRecordId, Long userId, String status) {
        this.pipelineId = pipelineId;
        this.auditRecordId = auditRecordId;
        this.userId = userId;
        this.status = status;
    }

    public Long getPipelineId() {
        return pipelineId;
    }

    public void setPipelineId(Long pipelineId) {
        this.pipelineId = pipelineId;
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

