package io.choerodon.devops.infra.dto;

import javax.persistence.Column;
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
 * ci 人工卡点配置表(CiAuditConfig)实体类
 *
 * @author hao.wang@zknow.com
 * @since 2022-11-02 11:40:27
 */

@ApiModel("ci 人工卡点配置表")
@VersionAudit
@ModifyAudit
@JsonInclude(value = JsonInclude.Include.NON_NULL)
@Table(name = "devops_ci_audit_config")
public class CiAuditConfigDTO extends AuditDomain {
    public static final String FIELD_ID = "id";
    public static final String FIELD_COUNTERSIGNED = "countersigned";
    private static final long serialVersionUID = 831700646155956076L;
    @Id
    @GeneratedValue
    private Long id;
    @ApiModelProperty(value = "关联devops流水线id")
    private Long ciPipelineId;

    @ApiModelProperty(value = "是否会签 1是会签,0 是或签", required = true)
    @NotNull
    @Column(name = "is_countersigned")
    private Boolean countersigned;

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

    public Boolean getCountersigned() {
        return countersigned;
    }

    public void setCountersigned(Boolean countersigned) {
        this.countersigned = countersigned;
    }
}

