package io.choerodon.devops.api.vo.iam.entity;

public class DevopsClusterProPermissionE {

    private Long clusterId;
    private Long projectId;

    public Long getClusterId() {
        return clusterId;
    }

    public void setClusterId(Long clusterId) {
        this.clusterId = clusterId;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }
}
