package io.choerodon.devops.api.vo;

import javax.validation.constraints.NotBlank;

import io.swagger.annotations.ApiModelProperty;

/**
 * @author hao.wang08@hand-china.com
 * @since 2022-03-24 17:04:47
 */
public class CommonScheduleVO {
    @ApiModelProperty(value = "触发类型：周期触发，单次触发", required = true)
    private String triggerType;
    @ApiModelProperty(value = "每周几触发", required = true)
    @NotBlank
    private String weekNumber;
    @ApiModelProperty(value = "开始时间：周期触发时需要，0-23")
    private Long startHourOfDay;
    @ApiModelProperty(value = "结束时间：周期触发时需要，0-23")
    private Long endHourOfDay;
    @ApiModelProperty(value = "执行间隔：周期触发时需要，10，20，30，40，50，60，120，240")
    private Long period;
    @ApiModelProperty(value = "执行时间：单次触发时需要")
    private String executeTime;

    public String getTriggerType() {
        return triggerType;
    }

    public void setTriggerType(String triggerType) {
        this.triggerType = triggerType;
    }

    public String getWeekNumber() {
        return weekNumber;
    }

    public void setWeekNumber(String weekNumber) {
        this.weekNumber = weekNumber;
    }

    public Long getStartHourOfDay() {
        return startHourOfDay;
    }

    public void setStartHourOfDay(Long startHourOfDay) {
        this.startHourOfDay = startHourOfDay;
    }

    public Long getEndHourOfDay() {
        return endHourOfDay;
    }

    public void setEndHourOfDay(Long endHourOfDay) {
        this.endHourOfDay = endHourOfDay;
    }

    public Long getPeriod() {
        return period;
    }

    public void setPeriod(Long period) {
        this.period = period;
    }

    public String getExecuteTime() {
        return executeTime;
    }

    public void setExecuteTime(String executeTime) {
        this.executeTime = executeTime;
    }
}
