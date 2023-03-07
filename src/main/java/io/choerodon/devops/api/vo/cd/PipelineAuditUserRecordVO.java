package io.choerodon.devops.api.vo.cd;

import io.swagger.annotations.ApiModelProperty;

/**
 * @author
 * @since 2022-11-24 16:56:55
 */
public class PipelineAuditUserRecordVO {


    private Long id;
    @ApiModelProperty(value = "devops_pipeline_audit_record.id", required = true)
    private Long auditRecordId;
    @ApiModelProperty(value = "用户Id", required = true)
    private Long userId;
    @ApiModelProperty(value = "人工审核的结果（待审核、拒绝、通过）", required = true)
    private String status;


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
