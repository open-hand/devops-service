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
 * 流水线阶段与任务模板的关系表(CiTemplateStageJobRel)实体类
 *
 * @author hao.wang08@hand-china.com
 * @since 2021-12-01 15:58:20
 */

@ApiModel("流水线阶段与任务模板的关系表")
@VersionAudit
@ModifyAudit
@JsonInclude(value = JsonInclude.Include.NON_NULL)
@Table(name = "devops_ci_template_stage_job_rel")
public class CiTemplateStageJobRelDTO extends AuditDomain {
    private static final long serialVersionUID = -76954587803624145L;

    public static final String FIELD_ID = "id";
    public static final String FIELD_CI_TEMPLATE_STAGE_ID = "ciTemplateStageId";
    public static final String FIELD_CI_TEMPLATE_JOB_ID = "ciTemplateJobId";

    @Id
    @GeneratedValue
    private Long id;

    @ApiModelProperty(value = "流水线模板阶段id", required = true)
    @NotNull
    private Long ciTemplateStageId;

    @ApiModelProperty(value = "流水线模板id", required = true)
    @NotNull
    private Long ciTemplateJobId;

    @Column(name = "is_enabled")
    private Boolean enabled;

    @ApiModelProperty(value = "顺序")
    private Integer sequence;

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCiTemplateStageId() {
        return ciTemplateStageId;
    }

    public void setCiTemplateStageId(Long ciTemplateStageId) {
        this.ciTemplateStageId = ciTemplateStageId;
    }

    public Long getCiTemplateJobId() {
        return ciTemplateJobId;
    }

    public void setCiTemplateJobId(Long ciTemplateJobId) {
        this.ciTemplateJobId = ciTemplateJobId;
    }

    public Integer getSequence() {
        return sequence;
    }

    public void setSequence(Integer sequence) {
        this.sequence = sequence;
    }
}

