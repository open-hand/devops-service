package io.choerodon.devops.api.dto;

import java.util.List;

public class DevopsEnvPreviewAppDTO {

    private String appName;
    private List<ApplicationInstanceDTO> applicationInstanceDTOS;

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public List<ApplicationInstanceDTO> getApplicationInstanceDTOS() {
        return applicationInstanceDTOS;
    }

    public void setApplicationInstanceDTOS(List<ApplicationInstanceDTO> applicationInstanceDTOS) {
        this.applicationInstanceDTOS = applicationInstanceDTOS;
    }
}
