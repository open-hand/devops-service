package io.choerodon.devops.api.vo;

import java.util.Date;
import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;

import io.swagger.annotations.ApiModelProperty;
import org.hzero.starter.keyencrypt.core.Encrypt;

import io.choerodon.devops.infra.dto.iam.IamUserDTO;
import io.choerodon.mybatis.domain.AuditDomain;

/**
 * @author hao.wang08@hand-china.com
 * @since 2022-03-24 17:04:47
 */
public class CiPipelineScheduleVO extends AuditDomain {


    @Encrypt
    private Long id;
    @ApiModelProperty(value = "应用服务id", required = true)
    @Encrypt
    private Long appServiceId;
    @ApiModelProperty(value = "定时任务名称", required = true)
    private String name;
    @ApiModelProperty(value = "gitlab pipeline_schedule_id", required = true)
    private Long pipelineScheduleId;
    @ApiModelProperty(value = "触发分支", required = true)
    private String ref;
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

    private IamUserDTO userDTO;

    private Date nextRunAt;
    private Boolean active;

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    @Valid
    List<CiScheduleVariableVO> variableVOList;


    public Date getNextRunAt() {
        return nextRunAt;
    }

    public void setNextRunAt(Date nextRunAt) {
        this.nextRunAt = nextRunAt;
    }

    public IamUserDTO getUserDTO() {
        return userDTO;
    }

    public void setUserDTO(IamUserDTO userDTO) {
        this.userDTO = userDTO;
    }

    public List<CiScheduleVariableVO> getVariableVOList() {
        return variableVOList;
    }

    public void setVariableVOList(List<CiScheduleVariableVO> variableVOList) {
        this.variableVOList = variableVOList;
    }

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
