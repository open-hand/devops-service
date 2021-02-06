package io.choerodon.devops.api.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import java.util.Date;
import org.hzero.starter.keyencrypt.core.Encrypt;

public class SagaInstanceDetails {
    @Encrypt
    @ApiModelProperty(value = "主键ID")
    private Long id;

    @ApiModelProperty(value = "对应Saga编码")
    private String sagaCode;

    @ApiModelProperty(value = "当前Saga 实例的状态")
    private String status;

    @ApiModelProperty(value = "对应Saga描述")
    private String description;

    @ApiModelProperty(value = "所属微服务")
    private String service;

    @ApiModelProperty(value = "触发层级")
    private String level;

    @ApiModelProperty(value = "关联业务类型")
    private String refType;

    @ApiModelProperty(value = "关联业务ID")
    private String refId;

    @ApiModelProperty(value = "开始时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date startTime;

    @ApiModelProperty(value = "结束时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date endTime;

    @ApiModelProperty(value = "实例下完成的任务个数")
    private Integer completedCount;
    @ApiModelProperty(value = "实例下运行的任务个数")
    private Integer runningCount;
    @ApiModelProperty(value = "实例下回滚的任务个数")
    private Integer rollbackCount;
    @ApiModelProperty(value = "实例下失败的任务个数")
    private Integer failedCount;
    @ApiModelProperty(value = "实例下等待被拉取里的任务个数")
    private Integer waitToBePulledCount;
    @ApiModelProperty("显示编码")
    private String viewId;

    public String getViewId() {
        return viewId;
    }

    public void setViewId(String viewId) {
        this.viewId = viewId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSagaCode() {
        return sagaCode;
    }

    public void setSagaCode(String sagaCode) {
        this.sagaCode = sagaCode;
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

    public String getRefType() {
        return refType;
    }

    public void setRefType(String refType) {
        this.refType = refType;
    }

    public String getRefId() {
        return refId;
    }

    public void setRefId(String refId) {
        this.refId = refId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public Integer getCompletedCount() {
        return completedCount;
    }

    public void setCompletedCount(Integer completedCount) {
        this.completedCount = completedCount;
    }

    public Integer getRunningCount() {
        return runningCount;
    }

    public void setRunningCount(Integer runningCount) {
        this.runningCount = runningCount;
    }

    public Integer getRollbackCount() {
        return rollbackCount;
    }

    public void setRollbackCount(Integer rollbackCount) {
        this.rollbackCount = rollbackCount;
    }

    public Integer getFailedCount() {
        return failedCount;
    }

    public void setFailedCount(Integer failedCount) {
        this.failedCount = failedCount;
    }

    public Integer getWaitToBePulledCount() {
        return waitToBePulledCount;
    }

    public void setWaitToBePulledCount(Integer waitToBePulledCount) {
        this.waitToBePulledCount = waitToBePulledCount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
