package io.choerodon.devops.api.vo;

import java.util.List;

/**
 * @author zmf
 */
public class DevopsClusterPodVO extends DevopsEnvPodVO {
    private List<DevopsEnvPodContainerLogVO> containersForLogs;

    public List<DevopsEnvPodContainerLogVO> getContainersForLogs() {
        return containersForLogs;
    }

    public void setContainersForLogs(List<DevopsEnvPodContainerLogVO> containersForLogs) {
        this.containersForLogs = containersForLogs;
    }
}
