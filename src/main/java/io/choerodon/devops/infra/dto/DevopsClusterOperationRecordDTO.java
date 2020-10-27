package io.choerodon.devops.infra.dto;

import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import io.swagger.annotations.ApiModelProperty;

import io.choerodon.mybatis.annotation.ModifyAudit;
import io.choerodon.mybatis.annotation.VersionAudit;
import io.choerodon.mybatis.domain.AuditDomain;

/**
 * @author lihao
 */
@ModifyAudit
@VersionAudit
@Table(name = "devops_cluster_operation_record")
public class DevopsClusterOperationRecordDTO extends AuditDomain {
    @Id
    @GeneratedValue
    private Long id;

    @ApiModelProperty(value = "操作对象类型")
    private String sourceType;

    @ApiModelProperty(value = "操作对象id")
    private Long sourceId;

    @ApiModelProperty(value = "操作状态")
    private String status;

    @ApiModelProperty(value = "失败日志")
    private String errorMsg;

    public Long getId() {
        return id;
    }

    public DevopsClusterOperationRecordDTO setId(Long id) {
        this.id = id;
        return this;
    }

    public String getSourceType() {
        return sourceType;
    }

    public DevopsClusterOperationRecordDTO setSourceType(String sourceType) {
        this.sourceType = sourceType;
        return this;
    }

    public Long getSourceId() {
        return sourceId;
    }

    public DevopsClusterOperationRecordDTO setSourceId(Long sourceId) {
        this.sourceId = sourceId;
        return this;
    }

    public String getStatus() {
        return status;
    }

    public DevopsClusterOperationRecordDTO setStatus(String status) {
        this.status = status;
        return this;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public DevopsClusterOperationRecordDTO setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
        return this;
    }

    @Override
    public String toString() {
        return "DevopsClusterOperationRecordDTO{" +
                "id=" + id +
                ", sourceType='" + sourceType + '\'' +
                ", sourceId=" + sourceId +
                ", status='" + status + '\'' +
                ", errorMsg='" + errorMsg + '\'' +
                '}';
    }
}
