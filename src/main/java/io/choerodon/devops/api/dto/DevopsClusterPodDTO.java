package io.choerodon.devops.api.dto;

import java.util.List;

/**
 * @author zmf
 */
public class DevopsClusterPodDTO extends DevopsEnvPodDTO {
    private List<DevopsEnvPodContainerLogDTO> containersForLogs;

    public List<DevopsEnvPodContainerLogDTO> getContainersForLogs() {
        return containersForLogs;
    }

    public void setContainersForLogs(List<DevopsEnvPodContainerLogDTO> containersForLogs) {
        this.containersForLogs = containersForLogs;
    }
}
