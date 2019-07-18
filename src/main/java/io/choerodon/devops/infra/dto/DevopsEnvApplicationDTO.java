package io.choerodon.devops.infra.dto;

import javax.persistence.Table;

@Table(name = "devops_env_application")
public class DevopsEnvApplicationDTO {
    private Long appId;
    private Long envId;

    public DevopsEnvApplicationDTO() {
    }

    public DevopsEnvApplicationDTO(Long appId, Long envId) {
        this.appId = appId;
        this.envId = envId;
    }

    public Long getAppId() {
        return appId;
    }

    public void setAppId(Long appId) {
        this.appId = appId;
    }

    public Long getEnvId() {
        return envId;
    }

    public void setEnvId(Long envId) {
        this.envId = envId;
    }
}
