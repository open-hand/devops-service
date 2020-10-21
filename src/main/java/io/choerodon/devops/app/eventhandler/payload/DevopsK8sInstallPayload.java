package io.choerodon.devops.app.eventhandler.payload;

import io.choerodon.devops.api.vo.DevopsClusterSshNodeInfoVO;

public class DevopsK8sInstallPayload {
    private Long projectId;
    private DevopsClusterSshNodeInfoVO devopsClusterSshNodeInfoVO;

    public Long getProjectId() {
        return projectId;
    }

    public DevopsK8sInstallPayload setProjectId(Long projectId) {
        this.projectId = projectId;
        return this;
    }

    public DevopsClusterSshNodeInfoVO getDevopsSshNodeInfoVO() {
        return devopsClusterSshNodeInfoVO;
    }

    public DevopsK8sInstallPayload setDevopsSshNodeInfoVO(DevopsClusterSshNodeInfoVO devopsClusterSshNodeInfoVO) {
        this.devopsClusterSshNodeInfoVO = devopsClusterSshNodeInfoVO;
        return this;
    }
}
