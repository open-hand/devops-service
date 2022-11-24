package io.choerodon.devops.api.vo.cd;

import io.swagger.annotations.ApiModelProperty;

/**
 * @author
 * @since 2022-11-24 16:56:37
 */
public class PipelineAuditUserVO {


    private Long id;
    @ApiModelProperty(value = "devops_pipeline_audit_cfg.id", required = true)
    private Long auditConfigId;
    @ApiModelProperty(value = "用户Id", required = true)
    private Long userId;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getAuditConfigId() {
        return auditConfigId;
    }

    public void setAuditConfigId(Long auditConfigId) {
        this.auditConfigId = auditConfigId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }
}
