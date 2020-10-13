package io.choerodon.devops.infra.dto.test;

import java.util.Date;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import io.swagger.annotations.ApiModelProperty;
import org.hzero.starter.keyencrypt.core.Encrypt;

import io.choerodon.mybatis.annotation.ModifyAudit;
import io.choerodon.mybatis.annotation.VersionAudit;
import io.choerodon.mybatis.domain.AuditDomain;

@ModifyAudit
@VersionAudit
@Table(name = "api_test_task_record")
public class ApiTestTaskRecordDTO extends AuditDomain {
    public static final String FIELD_ID = "id";

    @Encrypt
    @Id
    @GeneratedValue
    @ApiModelProperty(name = "主键")
    private Long id;

    @ApiModelProperty(name = "项目id")
    private Long projectId;
    @Encrypt
    @ApiModelProperty(name = "所属任务id")
    private Long taskId;

    @ApiModelProperty(name = "执行状态")
    private String status;

    @ApiModelProperty(name = "开始时间")
    private Date startTime;
    @ApiModelProperty(name = "结束时间")
    private Date endTime;

    @ApiModelProperty(name = "执行成功api数量")
    private Integer successCount;
    @ApiModelProperty(name = "执行失败api数量")
    private Integer failCount;

    @ApiModelProperty("执行结果压缩包文件url")
    private String resultFileUrl;

    @ApiModelProperty(name = "错误消息")
    private String errorMessage;

    @ApiModelProperty(name = "执行结果")
    @Transient
    private String result;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public Long getTaskId() {
        return taskId;
    }

    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
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

    public String getResultFileUrl() {
        return resultFileUrl;
    }

    public void setResultFileUrl(String resultFileUrl) {
        this.resultFileUrl = resultFileUrl;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    @Override
    public String toString() {
        return "ApiTestTaskRecordDTO{" +
                "id=" + id +
                ", projectId=" + projectId +
                ", taskId=" + taskId +
                ", status='" + status + '\'' +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                ", successCount=" + successCount +
                ", failCount=" + failCount +
                ", resultFileUrl='" + resultFileUrl + '\'' +
                ", errorMessage='" + errorMessage + '\'' +
                ", result='" + result + '\'' +
                '}';
    }
}
