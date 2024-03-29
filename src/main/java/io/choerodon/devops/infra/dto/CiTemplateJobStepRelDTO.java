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
 * 流水线任务模板与步骤模板关系表(CiTemplateJobStepRel)实体类
 *
 * @author hao.wang08@hand-china.com
 * @since 2021-12-01 15:58:17
 */

@ApiModel("流水线任务模板与步骤模板关系表")
@VersionAudit
@ModifyAudit
@JsonInclude(value = JsonInclude.Include.NON_NULL)
@Table(name = "devops_ci_template_job_step_rel")
public class CiTemplateJobStepRelDTO extends AuditDomain {
    private static final long serialVersionUID = 814297285130899205L;

    public static final String FIELD_ID = "id";
    public static final String FIELD_CI_TEMPLATE_JOB_ID = "ciTemplateJobId";
    public static final String FIELD_CI_TEMPLATE_STEP_ID = "ciTemplateStepId";
    public static final String FIELD_SEQUENCE = "sequence";
    public static final String FIELD_CI_TEMPLATE_STAGE_ID = "ciTemplateStageId";

    @Id
    @GeneratedValue
    private Long id;

    @ApiModelProperty(value = "层级Id", required = true)
    @NotNull
    private Long ciTemplateJobId;

    @ApiModelProperty(value = "层级Id", required = true)
    @NotNull
    private Long ciTemplateStepId;

    @ApiModelProperty(value = "顺序", required = true)
    @NotNull
    private Long sequence;



    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCiTemplateJobId() {
        return ciTemplateJobId;
    }

    public void setCiTemplateJobId(Long ciTemplateJobId) {
        this.ciTemplateJobId = ciTemplateJobId;
    }

    public Long getCiTemplateStepId() {
        return ciTemplateStepId;
    }

    public void setCiTemplateStepId(Long ciTemplateStepId) {
        this.ciTemplateStepId = ciTemplateStepId;
    }

    public Long getSequence() {
        return sequence;
    }

    public void setSequence(Long sequence) {
        this.sequence = sequence;
    }


}

