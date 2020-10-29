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

    @ApiModelProperty(value = "操作类型")
    private String type;

    @ApiModelProperty(value = "集群id")
    private Long clusterId;

    @ApiModelProperty(value = "节点id")
    private Long nodeId;

    @ApiModelProperty(value = "操作状态")
    private String status;

    @ApiModelProperty(value = "失败日志")
    private String errorMsg;

    public void appendErrorMsg(String errorMsg) {
        this.errorMsg += "\n" + errorMsg;
    }

    public Long getId() {
        return id;
    }

    public DevopsClusterOperationRecordDTO setId(Long id) {
        this.id = id;
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

    public String getType() {
        return type;
    }

    public DevopsClusterOperationRecordDTO setType(String type) {
        this.type = type;
        return this;
    }

    public Long getClusterId() {
        return clusterId;
    }

    public DevopsClusterOperationRecordDTO setClusterId(Long clusterId) {
        this.clusterId = clusterId;
        return this;
    }

    public Long getNodeId() {
        return nodeId;
    }

    public DevopsClusterOperationRecordDTO setNodeId(Long nodeId) {
        this.nodeId = nodeId;
        return this;
    }

    @Override
    public String toString() {
        return "DevopsClusterOperationRecordDTO{" +
                "id=" + id +
                ", type='" + type + '\'' +
                ", clusterId=" + clusterId +
                ", nodeId=" + nodeId +
                ", status='" + status + '\'' +
                ", errorMsg='" + errorMsg + '\'' +
                '}';
    }
}
