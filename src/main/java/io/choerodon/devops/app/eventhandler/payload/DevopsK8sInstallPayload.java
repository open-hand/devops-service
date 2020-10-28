package io.choerodon.devops.app.eventhandler.payload;

import io.choerodon.devops.api.vo.DevopsClusterNodeVO;

public class DevopsK8sInstallPayload {
    private Long operationRecordId;
    private Long projectId;
    private Long clusterId;
    private DevopsClusterNodeVO devopsClusterNodeVO;

    public Long getProjectId() {
        return projectId;
    }

    public DevopsK8sInstallPayload setProjectId(Long projectId) {
        this.projectId = projectId;
        return this;
    }

    public Long getClusterId() {
        return clusterId;
    }

    public DevopsK8sInstallPayload setClusterId(Long clusterId) {
        this.clusterId = clusterId;
        return this;
    }

    public DevopsClusterNodeVO getDevopsClusterNodeVO() {
        return devopsClusterNodeVO;
    }

    public DevopsK8sInstallPayload setDevopsClusterNodeVO(DevopsClusterNodeVO devopsClusterNodeVO) {
        this.devopsClusterNodeVO = devopsClusterNodeVO;
        return this;
    }

    public Long getOperationRecordId() {
        return operationRecordId;
    }

    public DevopsK8sInstallPayload setOperationRecordId(Long operationRecordId) {
        this.operationRecordId = operationRecordId;
        return this;
    }
}
