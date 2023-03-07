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
 * 流水线定时配置表(PipelineSchedule)实体类
 *
 * @author hao.wang@zknow.com
 * @since 2022-11-29 16:05:19
 */

@ApiModel("流水线定时配置表")
@VersionAudit
@ModifyAudit
@JsonInclude(value = JsonInclude.Include.NON_NULL)
@Table(name = "devops_pipeline_schedule")
public class PipelineScheduleDTO extends AuditDomain {
    public static final String FIELD_ID = "id";
    public static final String FIELD_PIPELINE_ID = "pipelineId";
    public static final String FIELD_NAME = "name";
    public static final String FIELD_TRIGGER_TYPE = "triggerType";
    public static final String FIELD_WEEK_NUMBER = "weekNumber";
    public static final String FIELD_START_HOUR_OF_DAY = "startHourOfDay";
    public static final String FIELD_END_HOUR_OF_DAY = "endHourOfDay";
    public static final String FIELD_PERIOD = "period";
    public static final String FIELD_EXECUTE_TIME = "executeTime";
    private static final long serialVersionUID = -62820471855573909L;
    @Id
    @GeneratedValue
    private Long id;

    @ApiModelProperty(value = "所属流水线id，devops_pipeline.id", required = true)
    @NotNull
    private Long pipelineId;

    @ApiModelProperty(value = "定时任务名称", required = true)
    @NotBlank
    private String name;

    @ApiModelProperty(value = "触发类型：周期触发，单次触发", required = true)
    @NotBlank
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

    @ApiModelProperty(value = "执行时间：单次触发时需要,HH:mm")
    private String executeTime;
    @ApiModelProperty(value = "定时执行时需要根据此token来判断是否可以执行")
    private String token;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getPipelineId() {
        return pipelineId;
    }

    public void setPipelineId(Long pipelineId) {
        this.pipelineId = pipelineId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

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

