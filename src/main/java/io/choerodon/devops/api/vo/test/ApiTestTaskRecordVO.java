package io.choerodon.devops.api.vo.test;

import java.util.Date;
import javax.persistence.Id;

import io.swagger.annotations.ApiModelProperty;
import org.hzero.starter.keyencrypt.core.Encrypt;

import io.choerodon.devops.infra.dto.iam.IamUserDTO;


public class ApiTestTaskRecordVO {
    @Encrypt
    @Id
    @ApiModelProperty(name = "主键")
    private Long id;

    @Encrypt
    @ApiModelProperty(name = "所属任务id")
    private Long taskId;

    @ApiModelProperty(name = "执行状态/failed/running/success")
    private String status;

    @ApiModelProperty(name = "开始时间")
    private Date startTime;

    @ApiModelProperty(name = "结束时间")
    private Date endTime;

    @ApiModelProperty(name = "执行成功api数量")
    private Integer successCount;

    @ApiModelProperty(name = "执行失败api数量")
    private Integer failCount;

    @ApiModelProperty("执行者名称")
    private IamUserDTO executorInfo;

    @ApiModelProperty(name = "错误消息")
    private String errorMessage;

    @ApiModelProperty("显示编号")
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

    public Long getTaskId() {
        return taskId;
    }

    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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

    public Integer getSuccessCount() {
        return successCount;
    }

    public void setSuccessCount(Integer successCount) {
        this.successCount = successCount;
    }

    public Integer getFailCount() {
        return failCount;
    }

    public void setFailCount(Integer failCount) {
        this.failCount = failCount;
    }

    public IamUserDTO getExecutorInfo() {
        return executorInfo;
    }

    public void setExecutorInfo(IamUserDTO executorInfo) {
        this.executorInfo = executorInfo;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}
