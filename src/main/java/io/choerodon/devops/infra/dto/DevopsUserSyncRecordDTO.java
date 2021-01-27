package io.choerodon.devops.infra.dto;

import java.util.Date;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import io.swagger.annotations.ApiModelProperty;

import io.choerodon.mybatis.annotation.ModifyAudit;
import io.choerodon.mybatis.annotation.VersionAudit;
import io.choerodon.mybatis.domain.AuditDomain;

/**
 * @author zmf
 * @since 2021/1/21
 */
@VersionAudit
@ModifyAudit
@Table(name = "devops_user_sync_record")
public class DevopsUserSyncRecordDTO extends AuditDomain {
    @Id
    @GeneratedValue
    private Long id;

    @ApiModelProperty(value = "开始时间")
    private Date startTime;
    @ApiModelProperty(value = "结束时间")
    private Date endTime;

    @ApiModelProperty(value = "同步成功用户数量")
    private Long successCount;
    @ApiModelProperty(value = "同步失败用户数量")
    private Long failCount;

    /**
     * {@link io.choerodon.devops.app.task.UserSyncTask}
     */
    @ApiModelProperty("记录的类型")
    private String type;

    /**
     * {@link io.choerodon.devops.infra.enums.UserSyncRecordStatus}
     */
    @ApiModelProperty("状态")
    private String status;

    @ApiModelProperty("失败的用户的详情")
    private String errorUserResultUrl;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public Long getSuccessCount() {
        return successCount;
    }

    public void setSuccessCount(Long successCount) {
        this.successCount = successCount;
    }

    public Long getFailCount() {
        return failCount;
    }

    public void setFailCount(Long failCount) {
        this.failCount = failCount;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getErrorUserResultUrl() {
        return errorUserResultUrl;
    }

    public void setErrorUserResultUrl(String errorUserResultUrl) {
        this.errorUserResultUrl = errorUserResultUrl;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
