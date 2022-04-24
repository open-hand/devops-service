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
 * devops_ci_pipeline_schedule(CiPipelineSchedule)实体类
 *
 * @author hao.wang08@hand-china.com
 * @since 2022-03-24 17:00:27
 */

@ApiModel("devops_ci_pipeline_schedule")
@VersionAudit
@ModifyAudit
@JsonInclude(value = JsonInclude.Include.NON_NULL)
@Table(name = "devops_ci_pipeline_schedule")
public class CiPipelineScheduleDTO extends AuditDomain {
    private static final long serialVersionUID = 469127561427345281L;

    public static final String FIELD_ID = "id";
    public static final String FIELD_APP_SERVICE_ID = "appServiceId";
    public static final String FIELD_NAME = "name";
    public static final String FIELD_PIPELINE_SCHEDULE_ID = "pipelineScheduleId";
    public static final String FIELD_REF = "ref";
    public static final String FIELD_TRIGGER_TYPE = "triggerType";
    public static final String FIELD_START_DAY_OF_HOUR = "startDayOfHour";
    public static final String FIELD_END_DAY_OF_HOUR = "endDayOfHour";
    public static final String FIELD_PERIOD = "period";
    public static final String FIELD_EXECUTE_TIME = "executeTime";

    @Id
    @GeneratedValue
    @Encrypt
    private Long id;

    @ApiModelProperty(value = "应用服务id", required = true)
    @NotNull
    @Encrypt
    private Long appServiceId;

    @ApiModelProperty(value = "定时任务名称", required = true)
    @NotBlank
    private String name;

    @ApiModelProperty(value = "gitlab pipeline_schedule_id", required = true)
    @NotNull
    private Long pipelineScheduleId;

    @ApiModelProperty(value = "触发分支", required = true)
    @NotBlank
    private String ref;

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

    @ApiModelProperty(value = "执行时间：单次触发时需要")
    private String executeTime;

    public String getWeekNumber() {
        return weekNumber;
    }

    public void setWeekNumber(String weekNumber) {
        this.weekNumber = weekNumber;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getAppServiceId() {
        return appServiceId;
    }

    public void setAppServiceId(Long appServiceId) {
        this.appServiceId = appServiceId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getPipelineScheduleId() {
        return pipelineScheduleId;
    }

    public void setPipelineScheduleId(Long pipelineScheduleId) {
        this.pipelineScheduleId = pipelineScheduleId;
    }

    public String getRef() {
        return ref;
    }

    public void setRef(String ref) {
        this.ref = ref;
    }

    public String getTriggerType() {
        return triggerType;
    }

    public void setTriggerType(String triggerType) {
        this.triggerType = triggerType;
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

