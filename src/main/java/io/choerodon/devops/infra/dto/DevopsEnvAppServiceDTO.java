package io.choerodon.devops.infra.dto;

import javax.persistence.Table;

@Table(name = "devops_env_app_service")
public class DevopsEnvAppServiceDTO {
    private Long appServiceId;
    private Long envId;

    public DevopsEnvAppServiceDTO() {
    }

    public DevopsEnvAppServiceDTO(Long appServiceId, Long envId) {
        this.appServiceId = appServiceId;
        this.envId = envId;
    }

    public Long getAppServiceId() {
        return appServiceId;
    }

    public void setAppServiceId(Long appServiceId) {
        this.appServiceId = appServiceId;
    }

    public Long getEnvId() {
        return envId;
    }

    public void setEnvId(Long envId) {
        this.envId = envId;
    }
}
