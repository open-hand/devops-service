package io.choerodon.devops.api.dto;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
    private String ports;
    @NotNull
    private List<Long> appInstance;
    private Map<String,String> label;

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

    public List<Long> getAppInstance() {
        return appInstance;
    }

    public void setAppInstance(List<Long> appInstance) {
        this.appInstance = appInstance;
    }

    public String getPorts() {
        return ports;
    }

    public void setPorts(String ports) {
        this.ports = ports;
    }

    public Map<String, String> getLabel() {
        return label;
    }

    public void setLabel(Map<String, String> labels) {
        this.label = labels;
    }
}
