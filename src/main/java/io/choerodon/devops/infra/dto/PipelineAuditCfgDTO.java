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
 * 人工卡点配置表(PipelineAuditConfig)实体类
 *
 * @author
 * @since 2022-11-24 15:56:37
 */

@ApiModel("人工卡点配置表")
@VersionAudit
@ModifyAudit
@JsonInclude(value = JsonInclude.Include.NON_NULL)
@Table(name = "devops_pipeline_audit_cfg")
public class PipelineAuditCfgDTO extends AuditDomain {
    private static final long serialVersionUID = 838513961051796101L;

    public static final String FIELD_ID = "id";
    public static final String FIELD_PIPELINE_ID = "pipelineId";
    public static final String FIELD_IS_COUNTERSIGNED = "isCountersigned";

    @Id
    @GeneratedValue
    private Long id;

    @ApiModelProperty(value = "流水线id", required = true)
    @NotNull
    private Long pipelineId;

    @ApiModelProperty(value = "是否会签 1是会签,0 是或签", required = true)
    @NotNull
    private Boolean isCountersigned;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getPipelineId() {
        return pipelineId;
    }

    public void setPipelineId(Long pipelineId) {
        this.pipelineId = pipelineId;
    }

    public Boolean getCountersigned() {
        return isCountersigned;
    }

    public void setCountersigned(Boolean countersigned) {
        isCountersigned = countersigned;
    }
}

