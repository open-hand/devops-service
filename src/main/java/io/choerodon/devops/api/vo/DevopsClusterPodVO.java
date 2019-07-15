package io.choerodon.devops.api.vo;

import java.util.List;

/**
 * @author zmf
 */
public class DevopsClusterPodVO extends DevopsEnvPodVO {
    private List<DevopsEnvPodContainerLogDTO> containersForLogs;

    public List<DevopsEnvPodContainerLogDTO> getContainersForLogs() {
        return containersForLogs;
    }

    public void setContainersForLogs(List<DevopsEnvPodContainerLogDTO> containersForLogs) {
        this.containersForLogs = containersForLogs;
    }
}
