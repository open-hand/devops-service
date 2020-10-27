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
@Table(name = "devops_cluster_node_operation_record")
public class DevopsClusterNodeOperationRecordDTO extends AuditDomain {
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

    public void setId(Long id) {
        this.id = id;
    }

    public String getSourceType() {
        return sourceType;
    }

    public void setSourceType(String sourceType) {
        this.sourceType = sourceType;
    }

    public Long getSourceId() {
        return sourceId;
    }

    public void setSourceId(Long sourceId) {
        this.sourceId = sourceId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }

    @Override
    public String toString() {
        return "DevopsClusterNodeOperationRecordDTO{" +
                "id=" + id +
                ", sourceType='" + sourceType + '\'' +
                ", sourceId=" + sourceId +
                ", status='" + status + '\'' +
                ", errorMsg='" + errorMsg + '\'' +
                '}';
    }
}
