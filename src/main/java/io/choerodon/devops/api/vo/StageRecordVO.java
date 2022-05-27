package io.choerodon.devops.api.vo;

import io.swagger.annotations.ApiModelProperty;

public class StageRecordVO {
    @ApiModelProperty("阶段名称")
    private String name;
    @ApiModelProperty("阶段顺序")
    private Long sequence;
    @ApiModelProperty("阶段的状态")
    private String status;
    @ApiModelProperty("阶段的类型")
    private String type;
    @ApiModelProperty("阶段执行耗时")
    private Long durationSeconds;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getSequence() {
        return sequence;
    }

    public void setSequence(Long sequence) {
        this.sequence = sequence;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Long getDurationSeconds() {
        return durationSeconds;
    }

    public void setDurationSeconds(Long durationSeconds) {
        this.durationSeconds = durationSeconds;
    }
}
