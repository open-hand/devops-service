package io.choerodon.devops.api.vo.test;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModelProperty;
import org.hzero.starter.keyencrypt.core.Encrypt;

/**
 * @author zmf
 * @since 2020/8/11
 */
public class ApiTestSuiteRecordSimpleVO {
    @Encrypt
    @ApiModelProperty("纪录id")
    private Long id;

    @ApiModelProperty("状态 / running / success / failed")
    private String status;

    @ApiModelProperty("开始时间")
    private Date creationDate;

    @ApiModelProperty("测试任务的触发类型 / 可为空")
    private String triggerType;

    @JsonIgnore
    @ApiModelProperty("创建者id")
    private Long createdBy;

    @ApiModelProperty("显示编码")
    private String viewId;

    @ApiModelProperty("成功的测试任务")
    private Integer successTask;

    @ApiModelProperty("总的测试任务")
    private Integer totalTask;

    @ApiModelProperty("错误信息")
    private String errorMessage;

    @ApiModelProperty(value = "开始时间")
    private Date startTime;
    @ApiModelProperty(value = "结束时间")
    private Date endTime;
    @ApiModelProperty(value = "执行耗时，单位秒")
    private Long executionTime;
    @ApiModelProperty(value = "执行模式,true串行,false并行")
    private Boolean serial;

    public Boolean getSerial() {
        return serial;
    }

    public void setSerial(Boolean serial) {
        this.serial = serial;
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

    public Long getExecutionTime() {
        return executionTime;
    }

    public void setExecutionTime(Long executionTime) {
        this.executionTime = executionTime;
    }

    public Integer getSuccessTask() {
        return successTask;
    }

    public void setSuccessTask(Integer successTask) {
        this.successTask = successTask;
    }

    public Integer getTotalTask() {
        return totalTask;
    }

    public void setTotalTask(Integer totalTask) {
        this.totalTask = totalTask;
    }

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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public Long getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Long createdBy) {
        this.createdBy = createdBy;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getTriggerType() {
        return triggerType;
    }

    public void setTriggerType(String triggerType) {
        this.triggerType = triggerType;
    }
}
