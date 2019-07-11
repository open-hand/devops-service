package io.choerodon.devops.api.vo;

import java.util.List;

public class DevopsEnvPreviewDTO {

    private List<DevopsEnvPreviewAppDTO> devopsEnvPreviewAppDTOS;

    public List<DevopsEnvPreviewAppDTO> getDevopsEnvPreviewAppDTOS() {
        return devopsEnvPreviewAppDTOS;
    }

    public void setDevopsEnvPreviewAppDTOS(List<DevopsEnvPreviewAppDTO> devopsEnvPreviewAppDTOS) {
        this.devopsEnvPreviewAppDTOS = devopsEnvPreviewAppDTOS;
    }
}
