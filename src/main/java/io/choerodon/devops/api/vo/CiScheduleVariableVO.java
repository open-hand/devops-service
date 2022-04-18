package io.choerodon.devops.api.vo;

import javax.validation.constraints.Pattern;

import io.swagger.annotations.ApiModelProperty;

/**
 * @author hao.wang08@hand-china.com
 * @since 2022-03-24 20:28:00
 */
public class CiScheduleVariableVO {


    private Long id;
    @ApiModelProperty(value = "devops 定时计划id", required = true)
    private Long ciPipelineScheduleId;
    @ApiModelProperty(value = "gitlab pipeline_schedule_id", required = true)
    private Long pipelineScheduleId;
    @ApiModelProperty(value = "key", required = true)
    @Pattern(regexp = "^[A-Za-z0-9_]{1,128}$", message = "error.variable.key.invalid")
    private String variableKey;
    @ApiModelProperty(value = "value", required = true)
    private String variableValue;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCiPipelineScheduleId() {
        return ciPipelineScheduleId;
    }

    public void setCiPipelineScheduleId(Long ciPipelineScheduleId) {
        this.ciPipelineScheduleId = ciPipelineScheduleId;
    }

    public Long getPipelineScheduleId() {
        return pipelineScheduleId;
    }

    public void setPipelineScheduleId(Long pipelineScheduleId) {
        this.pipelineScheduleId = pipelineScheduleId;
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
