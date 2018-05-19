package io.choerodon.devops.api.dto;

import java.util.Set;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * Created by Zenger on 2018/4/13.
 */
public class DevopsServiceReqDTO {

    @NotNull
    private Long envId;
    @NotNull
    private Long appId;
    @NotNull
    @Size(min = 1, max = 64, message = "error.name.size")
    private String name;
    private String externalIp;
    @NotNull
    private Long port;
    @NotNull
    private Set<Long> appInstance;

    public Long getEnvId() {
        return envId;
    }

    public void setEnvId(Long envId) {
        this.envId = envId;
    }

    public Long getAppId() {
        return appId;
    }

    public void setAppId(Long appId) {
        this.appId = appId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getExternalIp() {
        return externalIp;
    }

    public void setExternalIp(String externalIp) {
        this.externalIp = externalIp;
    }

    public Long getPort() {
        return port;
    }

    public void setPort(Long port) {
        this.port = port;
    }

    public Set<Long> getAppInstance() {
        return appInstance;
    }

    public void setAppInstance(Set<Long> appInstance) {
        this.appInstance = appInstance;
    }
}
