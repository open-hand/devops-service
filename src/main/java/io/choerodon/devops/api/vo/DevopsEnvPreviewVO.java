package io.choerodon.devops.api.vo;

import java.util.List;

public class DevopsEnvPreviewVO {

    private List<DevopsEnvPreviewAppVO> devopsEnvPreviewAppVOS;

    public List<DevopsEnvPreviewAppVO> getDevopsEnvPreviewAppVOS() {
        return devopsEnvPreviewAppVOS;
    }

    public void setDevopsEnvPreviewAppVOS(List<DevopsEnvPreviewAppVO> devopsEnvPreviewAppVOS) {
        this.devopsEnvPreviewAppVOS = devopsEnvPreviewAppVOS;
    }
}
