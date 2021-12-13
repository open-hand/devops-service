package io.choerodon.devops.api.vo.template;

import io.swagger.annotations.ApiModelProperty;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 流水线模板配置的CI变量(CiTemplateVariable)实体类
 *
 * @author hao.wang08@hand-china.com
 * @since 2021-12-01 15:58:21
 */

public class CiTemplateVariableVO {

    @Id
    @GeneratedValue
    private Long id;

    @ApiModelProperty(value = "流水线模板id", required = true)
    @NotNull
    private Long pipelineTemplateId;

    @ApiModelProperty(value = "层级", required = true)
    @NotBlank
    private String variableKey;

    @ApiModelProperty(value = "层级", required = true)
    @NotBlank
    private String variableValue;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getPipelineTemplateId() {
        return pipelineTemplateId;
    }

    public void setPipelineTemplateId(Long pipelineTemplateId) {
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

