package io.choerodon.devops.api.dto;

import java.util.List;

public class DevopsEnvPreviewAppDTO {

    private String appName;
    private String appCode;
    private List<DevopsEnvPreviewInstanceDTO> applicationInstanceDTOS;

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getAppCode() {
        return appCode;
    }

    public void setAppCode(String appCode) {
        this.appCode = appCode;
    }

    public List<DevopsEnvPreviewInstanceDTO> getApplicationInstanceDTOS() {
        return applicationInstanceDTOS;
    }

    public void setApplicationInstanceDTOS(List<DevopsEnvPreviewInstanceDTO> applicationInstanceDTOS) {
        this.applicationInstanceDTOS = applicationInstanceDTOS;
    }
}
