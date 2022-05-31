package io.choerodon.devops.api.vo.workload;

import java.util.List;
import java.util.Map;

import io.swagger.annotations.ApiModelProperty;
import org.hzero.starter.keyencrypt.core.Encrypt;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2021/6/11 0:09
 */
public class CronJobInfoVO {
    @Encrypt
    private Long id;
    @ApiModelProperty("cronjob名称")
    private String name;
    @ApiModelProperty("执行计划")
    private String schedule;
    @ApiModelProperty("是否暂停")
    private Boolean suspend;
    @ApiModelProperty("是否启用")
    private Integer active;
    @ApiModelProperty("cronjob创建时间")
    private String creationTimestamp;
    @ApiModelProperty("cronjob最近执行时间")
    private String lastScheduleTime;
    @ApiModelProperty("cronjob端口号")
    private List<Integer> ports;
    @ApiModelProperty("cronjob标签")
    private Map<String, String> labels;
    @Encrypt
    @ApiModelProperty("cronjob所属实例id")
    private Long instanceId;
    @ApiModelProperty("commandType")
    private String commandType;
    @ApiModelProperty("commandStatus")
    private String commandStatus;
    @ApiModelProperty("错误信息")
    private String error;

    @ApiModelProperty("来源类型 chart/工作负载")
    private String sourceType;

    public String getCreationTimestamp() {
        return creationTimestamp;
    }

    public void setCreationTimestamp(String creationTimestamp) {
        this.creationTimestamp = creationTimestamp;
    }

    public String getSchedule() {
        return schedule;
    }

    public void setSchedule(String schedule) {
        this.schedule = schedule;
    }

    public Boolean getSuspend() {
        return suspend;
    }

    public void setSuspend(Boolean suspend) {
        this.suspend = suspend;
    }

    public Integer getActive() {
        return active;
    }

    public void setActive(Integer active) {
        this.active = active;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLastScheduleTime() {
        return lastScheduleTime;
    }

    public void setLastScheduleTime(String lastScheduleTime) {
        this.lastScheduleTime = lastScheduleTime;
    }

    public List<Integer> getPorts() {
        return ports;
    }

    public void setPorts(List<Integer> ports) {
        this.ports = ports;
    }

    public Map<String, String> getLabels() {
        return labels;
    }

    public void setLabels(Map<String, String> labels) {
        this.labels = labels;
    }

    public Long getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(Long instanceId) {
        this.instanceId = instanceId;
    }

    public String getCommandType() {
        return commandType;
    }

    public void setCommandType(String commandType) {
        this.commandType = commandType;
    }

    public String getCommandStatus() {
        return commandStatus;
    }

    public void setCommandStatus(String commandStatus) {
        this.commandStatus = commandStatus;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getSourceType() {
        return sourceType;
    }

    public void setSourceType(String sourceType) {
        this.sourceType = sourceType;
    }
}
