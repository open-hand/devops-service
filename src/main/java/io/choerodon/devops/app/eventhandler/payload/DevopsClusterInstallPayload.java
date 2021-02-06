package io.choerodon.devops.app.eventhandler.payload;

import io.swagger.annotations.ApiModelProperty;

public class DevopsClusterInstallPayload {
    @ApiModelProperty("操作记录id")
    private Long operationRecordId;
    @ApiModelProperty("项目id")
    private Long projectId;
    @ApiModelProperty("集群id")
    private Long clusterId;
    @ApiModelProperty("保存日志的redisKey")
    private String redisKey;
    @ApiModelProperty("保存集群和节点信息的redisKey")
    private String clusterInfoRedisKey;


    public Long getProjectId() {
        return projectId;
    }

    public DevopsClusterInstallPayload setProjectId(Long projectId) {
        this.projectId = projectId;
        return this;
    }

    public Long getClusterId() {
        return clusterId;
    }

    public DevopsClusterInstallPayload setClusterId(Long clusterId) {
        this.clusterId = clusterId;
        return this;
    }

    public Long getOperationRecordId() {
        return operationRecordId;
    }

    public DevopsClusterInstallPayload setOperationRecordId(Long operationRecordId) {
        this.operationRecordId = operationRecordId;
        return this;
    }

    public String getRedisKey() {
        return redisKey;
    }

    public DevopsClusterInstallPayload setRedisKey(String redisKey) {
        this.redisKey = redisKey;
        return this;
    }

    public String getClusterInfoRedisKey() {
        return clusterInfoRedisKey;
    }

    public DevopsClusterInstallPayload setClusterInfoRedisKey(String clusterInfoRedisKey) {
        this.clusterInfoRedisKey = clusterInfoRedisKey;
        return this;
    }
}
