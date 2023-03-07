package io.choerodon.devops.infra.dto;

import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import io.choerodon.mybatis.annotation.ModifyAudit;
import io.choerodon.mybatis.annotation.VersionAudit;
import io.choerodon.mybatis.domain.AuditDomain;

/**
 * 人工卡点审核人员表(PipelineAuditUser)实体类
 *
 * @author
 * @since 2022-11-24 15:56:48
 */

@ApiModel("人工卡点审核人员表")
@VersionAudit
@ModifyAudit
@JsonInclude(value = JsonInclude.Include.NON_NULL)
@Table(name = "devops_pipeline_audit_user")
public class PipelineAuditUserDTO extends AuditDomain {
    private static final long serialVersionUID = 420198525105606920L;

    public static final String FIELD_ID = "id";
    public static final String FIELD_AUDIT_CONFIG_ID = "auditConfigId";
    public static final String FIELD_USER_ID = "userId";

    @Id
    @GeneratedValue
    private Long id;

    @ApiModelProperty(value = "devops_pipeline_audit_cfg.id", required = true)
    @NotNull
    private Long auditConfigId;

    @ApiModelProperty(value = "用户Id", required = true)
    @NotNull
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

