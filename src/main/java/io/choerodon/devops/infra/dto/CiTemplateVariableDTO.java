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
 * 流水线模板配置的CI变量(CiTemplateVariable)实体类
 *
 * @author hao.wang08@hand-china.com
 * @since 2021-12-01 15:58:21
 */

@ApiModel("流水线模板配置的CI变量")
@VersionAudit
@ModifyAudit
@JsonInclude(value = JsonInclude.Include.NON_NULL)
@Table(name = "devops_ci_template_variable")
public class CiTemplateVariableDTO extends AuditDomain {
    private static final long serialVersionUID = 149759544385612001L;

    public static final String FIELD_ID = "id";
    public static final String FIELD_PIPELINE_TEMPLATE_ID = "pipelineTemplateId";
    public static final String FIELD_VARIABLE_KEY = "variableKey";
    public static final String FIELD_VARIABLE_VALUE = "variableValue";

    @Id
    @GeneratedValue
    private Object id;

    @ApiModelProperty(value = "流水线模板id", required = true)
    @NotNull
    private Object pipelineTemplateId;

    @ApiModelProperty(value = "层级", required = true)
    @NotBlank
    private String variableKey;

    @ApiModelProperty(value = "层级", required = true)
    @NotBlank
    private String variableValue;


    public Object getId() {
        return id;
    }

    public void setId(Object id) {
        this.id = id;
    }

    public Object getPipelineTemplateId() {
        return pipelineTemplateId;
    }

    public void setPipelineTemplateId(Object pipelineTemplateId) {
        this.pipelineTemplateId = pipelineTemplateId;
    }

    public String getVariableKey() {
        return variableKey;
    }

    public void setVariableKey(String variableKey) {
        this.variableKey = variableKey;
    }

    public String getVariableValue() {
        return variableValue;
    }

    public void setVariableValue(String variableValue) {
        this.variableValue = variableValue;
    }

}

