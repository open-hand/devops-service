package io.choerodon.devops.api.vo;

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
    private String key;
    @ApiModelProperty(value = "value", required = true)
    private String value;


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

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
