package io.choerodon.devops.app.eventhandler.payload;

import io.choerodon.devops.api.vo.DevopsClusterNodeVO;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2020/11/1 23:54
 */
public class DevopsAddNodePayload {
    private Long projectId;
    private Long clusterId;
    private DevopsClusterNodeVO nodeVO;
    private String operatingId;

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public Long getClusterId() {
        return clusterId;
    }

    public void setClusterId(Long clusterId) {
        this.clusterId = clusterId;
    }

    public DevopsClusterNodeVO getNodeVO() {
        return nodeVO;
    }

    public void setNodeVO(DevopsClusterNodeVO nodeVO) {
        this.nodeVO = nodeVO;
    }

    public String getOperatingId() {
        return operatingId;
    }

    public void setOperatingId(String operatingId) {
        this.operatingId = operatingId;
    }

    @Override
    public String toString() {
        return "DevopsAddNodePayload{" +
                "projectId=" + projectId +
                ", clusterId=" + clusterId +
                ", nodeVO=" + nodeVO +
                ", operatingId='" + operatingId + '\'' +
                '}';
    }
}
