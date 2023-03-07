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
 * 流水线模阶段(CiTemplateStage)实体类
 *
 * @author hao.wang08@hand-china.com
 * @since 2021-12-01 15:58:19
 */

@ApiModel("流水线模阶段")
@VersionAudit
@ModifyAudit
@JsonInclude(value = JsonInclude.Include.NON_NULL)
@Table(name = "devops_ci_template_stage")
public class CiTemplateStageDTO extends AuditDomain {
    private static final long serialVersionUID = -68322700971096397L;

    public static final String FIELD_ID = "id";
    public static final String FIELD_NAME = "name";
    public static final String FIELD_PIPELINE_TEMPLATE_ID = "pipelineTemplateId";

    @Id
    @GeneratedValue
    private Long id;

    @ApiModelProperty(value = "阶段名称", required = true)
    @NotBlank
    private String name;

    @ApiModelProperty(value = "流水线模板id", required = true)
    @NotNull
    private Long pipelineTemplateId;

    @ApiModelProperty(value = "顺序", required = true)
    @NotNull
    private Long sequence;

    @ApiModelProperty("任务模板是否可见")
    private Boolean visibility;

    public Long getSequence() {
        return sequence;
    }

    public void setSequence(Long sequence) {
        this.sequence = sequence;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getPipelineTemplateId() {
        return pipelineTemplateId;
    }

    public void setPipelineTemplateId(Long pipelineTemplateId) {
        this.pipelineTemplateId = pipelineTemplateId;
    }

    public Boolean getVisibility() {
        return visibility;
    }

    public void setVisibility(Boolean visibility) {
        this.visibility = visibility;
    }
}

