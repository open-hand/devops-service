package io.choerodon.devops.app.eventhandler.payload;

import io.choerodon.devops.api.vo.DevopsClusterNodeVO;

public class DevopsClusterOperationPayload {
    private Long operationRecordId;
    private Long projectId;
    private Long clusterId;
    private DevopsClusterNodeVO devopsClusterNodeVO;

    public Long getProjectId() {
        return projectId;
    }

    public DevopsClusterOperationPayload setProjectId(Long projectId) {
        this.projectId = projectId;
        return this;
    }

    public Long getClusterId() {
        return clusterId;
    }

    public DevopsClusterOperationPayload setClusterId(Long clusterId) {
        this.clusterId = clusterId;
        return this;
    }

    public DevopsClusterNodeVO getDevopsClusterNodeVO() {
        return devopsClusterNodeVO;
    }

    public DevopsClusterOperationPayload setDevopsClusterNodeVO(DevopsClusterNodeVO devopsClusterNodeVO) {
        this.devopsClusterNodeVO = devopsClusterNodeVO;
        return this;
    }

    public Long getOperationRecordId() {
        return operationRecordId;
    }

    public DevopsClusterOperationPayload setOperationRecordId(Long operationRecordId) {
        this.operationRecordId = operationRecordId;
        return this;
    }
}
