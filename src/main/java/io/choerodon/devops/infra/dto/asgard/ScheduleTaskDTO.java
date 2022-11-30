package io.choerodon.devops.infra.dto.asgard;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

import io.swagger.annotations.ApiModelProperty;
import org.hzero.starter.keyencrypt.core.Encrypt;

public class ScheduleTaskDTO {

    private Long projectId;

    private String operationType;

    @ApiModelProperty(value = "执行任务方法id")
    private Long methodId;

    @ApiModelProperty(value = "执行任务serviceCode")
    private String serviceCode;

    @ApiModelProperty(value = "执行任务methodCode")
    private String methodCode;

    @ApiModelProperty(value = "输入参数的map形式")
    private Map<String, Object> params;

    @ApiModelProperty(value = "定时任务名")
    @NotEmpty(message = "error.scheduleTask.nameEmpty")
    @Size(max = 255, message = "error.scheduleTask.name.size")
    private String name;

    @ApiModelProperty(value = "定时任务描述")
    private String description;

    @ApiModelProperty(value = "定时任务开始时间")
    private Date startTime;

    @ApiModelProperty(value = "定时任务结束时间")
    private Date endTime;

    @ApiModelProperty(value = "定时任务结束类型。simple-trigger或cron-trigger")
    @NotEmpty(message = "error.scheduleTask.triggerTypeEmpty")
    private String triggerType;

    @ApiModelProperty(value = "simple-trigger的重复次数")
    private Integer simpleRepeatCount;

    @ApiModelProperty(value = "simple-trigger的重复间隔")
    private Long simpleRepeatInterval;

    @ApiModelProperty(value = "simple-trigger的重复间隔单位")
    private String simpleRepeatIntervalUnit;

    @ApiModelProperty(value = "cron-trigger的cron表达式")
    private String cronExpression;

    @ApiModelProperty(value = "执行策略")
    private String executeStrategy;

    @ApiModelProperty(value = "启用/禁用状态")
    private String status;

    private NotifyUser notifyUser;

    @Encrypt
    private Long[] assignUserIds;

    @ApiModelProperty(value = "用于feign传递时间，相差8小时,以后若解决可删除")
    private String startTimeStr;

    public ScheduleTaskDTO() {
        this.methodId = 0L;
    }

    public ScheduleTaskDTO(Long methodId, Map<String, Object> params, String name, String description, Date startTime) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yy/MM/dd HH:mm:ss");
        this.methodId = methodId;
        this.params = params;
        this.name = name + "-" + simpleDateFormat.format(new Date());
        this.description = description;
        this.startTime = startTime;
        this.triggerType = "simple-trigger";
        this.simpleRepeatCount = 0;
        this.simpleRepeatInterval = 3600L;
        this.simpleRepeatIntervalUnit = "SECONDS";
        this.endTime = null;
        this.cronExpression = null;
    }


    public String getOperationType() {
        return operationType;
    }

    public void setOperationType(String operationType) {
        this.operationType = operationType;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public String getServiceCode() {
        return serviceCode;
    }

    public void setServiceCode(String serviceCode) {
        this.serviceCode = serviceCode;
    }

    public String getMethodCode() {
        return methodCode;
    }

    public void setMethodCode(String methodCode) {
        this.methodCode = methodCode;
    }

    public NotifyUser getNotifyUser() {
        return notifyUser;
    }

    public void setNotifyUser(NotifyUser notifyUser) {
        this.notifyUser = notifyUser;
    }

    public Long[] getAssignUserIds() {
        return assignUserIds;
    }

    public void setAssignUserIds(Long[] assignUserIds) {
        this.assignUserIds = assignUserIds;
    }

    public Long getMethodId() {
        return methodId;
    }

    public void setMethodId(Long methodId) {
        this.methodId = methodId;
    }

    public Map<String, Object> getParams() {
        return params;
    }

    public void setParams(Map<String, Object> params) {
        this.params = params;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public String getTriggerType() {
        return triggerType;
    }

    public void setTriggerType(String triggerType) {
        this.triggerType = triggerType;
    }

    public Integer getSimpleRepeatCount() {
        return simpleRepeatCount;
    }

    public void setSimpleRepeatCount(Integer simpleRepeatCount) {
        this.simpleRepeatCount = simpleRepeatCount;
    }

    public Long getSimpleRepeatInterval() {
        return simpleRepeatInterval;
    }

    public void setSimpleRepeatInterval(Long simpleRepeatInterval) {
        this.simpleRepeatInterval = simpleRepeatInterval;
    }

    public String getCronExpression() {
        return cronExpression;
    }

    public void setCronExpression(String cronExpression) {
        this.cronExpression = cronExpression;
    }

    public String getSimpleRepeatIntervalUnit() {
        return simpleRepeatIntervalUnit;
    }

    public void setSimpleRepeatIntervalUnit(String simpleRepeatIntervalUnit) {
        this.simpleRepeatIntervalUnit = simpleRepeatIntervalUnit;
    }

    public String getExecuteStrategy() {
        return executeStrategy;
    }

    public void setExecuteStrategy(String executeStrategy) {
        this.executeStrategy = executeStrategy;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStartTimeStr() {
        return startTimeStr;
    }

    public void setStartTimeStr(String startTimeStr) {
        this.startTimeStr = startTimeStr;
    }

    public enum TriggerEventStrategy {
        STOP,
        SERIAL,
        PARALLEL;

        TriggerEventStrategy() {
        }
    }

    public static class NotifyUser {
        boolean creator;
        boolean administrator;
        boolean assigner;

        public boolean getCreator() {
            return creator;
        }

        public void setCreator(boolean creator) {
            this.creator = creator;
        }

        public boolean getAdministrator() {
            return administrator;
        }

        public void setAdministrator(boolean administrator) {
            this.administrator = administrator;
        }

        public boolean getAssigner() {
            return assigner;
        }

        public void setAssigner(boolean assigner) {
            this.assigner = assigner;
        }
    }
}
