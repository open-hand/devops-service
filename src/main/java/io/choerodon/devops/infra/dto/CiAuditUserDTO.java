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
 * ci 人工卡点审核人员表(CiAuditUser)实体类
 *
 * @author hao.wang@zknow.com
 * @since 2022-11-02 11:40:40
 */

@ApiModel("ci 人工卡点审核人员表")
@VersionAudit
@ModifyAudit
@JsonInclude(value = JsonInclude.Include.NON_NULL)
@Table(name = "devops_ci_audit_user")
public class CiAuditUserDTO extends AuditDomain {
    public static final String FIELD_ID = "id";
    public static final String FIELD_AUDIT_CONFIG_ID = "auditConfigId";
    public static final String FIELD_USER_ID = "userId";
    private static final long serialVersionUID = 957760331805587212L;
    @Id
    @GeneratedValue
    private Long id;

    @ApiModelProperty(value = "devops_ci_audit_config.id", required = true)
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

