package io.choerodon.devops.api.vo;

import java.util.List;

/**
 * Created by Zenger on 2018/4/19.
 */
public class DevopsServiceDTO {

    private Long id;
    private String name;
    private String status;
    private Long envId;
    private String envName;
    private String type;
    private Boolean envStatus;
    private Long appId;
    private Long appProjectId;
    private String appName;
    private DevopsServiceTargetDTO target;
    private DevopsServiceConfigDTO config;
    private String commandType;
    private String commandStatus;
    private String error;
    private String loadBalanceIp;
    private List<DevopsIngressDTO> devopsIngressDTOS;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getEnvId() {
        return envId;
    }

    public void setEnvId(Long envId) {
        this.envId = envId;
    }

    public String getEnvName() {
        return envName;
    }

    public void setEnvName(String envName) {
        this.envName = envName;
    }

    public Long getAppId() {
        return appId;
    }

    public void setAppId(Long appId) {
        this.appId = appId;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Boolean getEnvStatus() {
        return envStatus;
    }

    public void setEnvStatus(Boolean envStatus) {
        this.envStatus = envStatus;
    }

    public Long getAppProjectId() {
        return appProjectId;
    }

    public void setAppProjectId(Long appProjectId) {
        this.appProjectId = appProjectId;
    }

    public DevopsServiceTargetDTO getTarget() {
        return target;
    }

    public void setTarget(DevopsServiceTargetDTO target) {
        this.target = target;
    }

    public DevopsServiceConfigDTO getConfig() {
        return config;
    }

    public void setConfig(DevopsServiceConfigDTO config) {
        this.config = config;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getCommandType() {
        return commandType;
    }

    public void setCommandType(String commandType) {
        this.commandType = commandType;
    }

    public String getCommandStatus() {
        return commandStatus;
    }

    public void setCommandStatus(String commandStatus) {
        this.commandStatus = commandStatus;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getLoadBalanceIp() {
        return loadBalanceIp;
    }

    public void setLoadBalanceIp(String loadBalanceIp) {
        this.loadBalanceIp = loadBalanceIp;
    }

    public List<DevopsIngressDTO> getDevopsIngressDTOS() {
        return devopsIngressDTOS;
    }

    public void setDevopsIngressDTOS(List<DevopsIngressDTO> devopsIngressDTOS) {
        this.devopsIngressDTOS = devopsIngressDTOS;
    }
}
