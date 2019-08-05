package io.choerodon.devops.api.vo;

import java.util.List;

public class DevopsEnvPreviewAppVO {

    private String appServiceName;
    private String appServiceCode;
    private Long projectId;
    private List<AppServiceInstanceVO> appServiceInstanceVOS;

    public String getAppServiceName() {
        return appServiceName;
    }

    public void setAppServiceName(String appServiceName) {
        this.appServiceName = appServiceName;
    }

    public String getAppServiceCode() {
        return appServiceCode;
    }

    public void setAppServiceCode(String appServiceCode) {
        this.appServiceCode = appServiceCode;
    }

    public List<AppServiceInstanceVO> getAppServiceInstanceVOS() {
        return appServiceInstanceVOS;
    }

    public void setAppServiceInstanceVOS(List<AppServiceInstanceVO> appServiceInstanceVOS) {
        this.appServiceInstanceVOS = appServiceInstanceVOS;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }
}
