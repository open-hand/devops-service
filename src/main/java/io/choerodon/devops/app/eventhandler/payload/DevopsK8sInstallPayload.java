package io.choerodon.devops.app.eventhandler.payload;

import io.choerodon.devops.api.vo.DevopsClusterSshNodeInfoVO;

public class DevopsK8sInstallPayload {
    private Long projectId;
    private Long clusterId;
    private DevopsClusterSshNodeInfoVO devopsClusterSshNodeInfoVO;

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

    public DevopsClusterSshNodeInfoVO getDevopsClusterSshNodeInfoVO() {
        return devopsClusterSshNodeInfoVO;
    }

    public DevopsK8sInstallPayload setDevopsClusterSshNodeInfoVO(DevopsClusterSshNodeInfoVO devopsClusterSshNodeInfoVO) {
        this.devopsClusterSshNodeInfoVO = devopsClusterSshNodeInfoVO;
        return this;
    }
}
