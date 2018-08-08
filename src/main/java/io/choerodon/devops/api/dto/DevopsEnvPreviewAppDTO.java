package io.choerodon.devops.api.dto;

import java.util.List;

public class DevopsEnvPreviewAppDTO {

    private String appName;
    private List<DevopsEnvPreviewInstanceDTO> applicationInstanceDTOS;

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public List<DevopsEnvPreviewInstanceDTO> getApplicationInstanceDTOS() {
        return applicationInstanceDTOS;
    }

    public void setApplicationInstanceDTOS(List<DevopsEnvPreviewInstanceDTO> applicationInstanceDTOS) {
        this.applicationInstanceDTOS = applicationInstanceDTOS;
    }
}
