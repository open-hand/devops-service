package io.choerodon.devops.infra.dataobject;

import javax.persistence.Table;

@Table(name = "devops_env_application")
public class DevopsEnvApplicationDO {
    private Long appId;
    private Long envId;

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
