package io.choerodon.devops.api.dto;

import java.util.List;

public class DevopsEnvPreviewAppDTO {

    private String appName;
    private List<DevopsEnvPreviewInstanceDTO> devopsEnvPreviewInstanceDTOS;

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public List<DevopsEnvPreviewInstanceDTO> getDevopsEnvPreviewInstanceDTOS() {
        return devopsEnvPreviewInstanceDTOS;
    }

    public void setDevopsEnvPreviewInstanceDTOS(List<DevopsEnvPreviewInstanceDTO> devopsEnvPreviewInstanceDTOS) {
        this.devopsEnvPreviewInstanceDTOS = devopsEnvPreviewInstanceDTOS;
    }
}
