package io.choerodon.devops.infra.dto;

import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.hzero.starter.keyencrypt.core.Encrypt;

import io.choerodon.mybatis.annotation.ModifyAudit;
import io.choerodon.mybatis.annotation.VersionAudit;
import io.choerodon.mybatis.domain.AuditDomain;

/**
 * 流水线配置的CI变量(DevopsCiPipelineVariable)实体类
 *
 * @author hao.wang08@hand-china.com
 * @since 2021-12-03 16:18:15
 */

@ApiModel("流水线配置的CI变量")
@VersionAudit
@ModifyAudit
@JsonInclude(value = JsonInclude.Include.NON_NULL)
@Table(name = "devops_ci_pipeline_variable")
public class DevopsCiPipelineVariableDTO extends AuditDomain {
    public static final String FIELD_ID = "id";
    public static final String FIELD_PIPELINE_ID = "pipelineId";
    public static final String FIELD_VARIABLE_KEY = "variableKey";
    public static final String FIELD_VARIABLE_VALUE = "variableValue";
    private static final long serialVersionUID = -56116170534172667L;
    @Id
    @GeneratedValue
    @Encrypt
    private Long id;

    @ApiModelProperty(value = "流水线id", required = true)
    @NotNull
    @Encrypt
    private Long devopsPipelineId;

    @ApiModelProperty(value = "变量名", required = true)
    @NotBlank
    private String variableKey;

    @ApiModelProperty(value = "变量值", required = true)
    @NotBlank
    private String variableValue;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getDevopsPipelineId() {
        return devopsPipelineId;
    }

    public void setDevopsPipelineId(Long devopsPipelineId) {
        this.devopsPipelineId = devopsPipelineId;
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

