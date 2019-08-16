package io.choerodon.devops.api.vo;

import java.util.List;
import java.util.Map;

import io.swagger.annotations.ApiModelProperty;

/**
 * Created by Zenger on 2018/4/19.
 */
public class DevopsServiceVO extends DevopsResourceDataInfoVO {

    private Long id;
    private String name;
    private String status;
    private Long envId;
    private String envName;
    private String type;
    private Boolean envStatus;
    private Long appServiceId;
    private Long appServiceProjectId;
    private String appServiceName;
    private String dns;
    /**
     * 网络本身的标签
     */
    @ApiModelProperty("网络本身的标签")
    private Map<String, String> labels;
    private DevopsServiceTargetVO target;
    private DevopsServiceConfigVO config;
    private String commandType;
    private String commandStatus;
    private String error;
    private String loadBalanceIp;
    private List<DevopsIngressVO> devopsIngressVOS;

    /**
     * pod实时信息
     */
    @ApiModelProperty("pod实时信息")
    private List<PodLiveInfoVO> podLiveInfos;

    public List<PodLiveInfoVO> getPodLiveInfos() {
        return podLiveInfos;
    }

    public void setPodLiveInfos(List<PodLiveInfoVO> podLiveInfos) {
        this.podLiveInfos = podLiveInfos;
    }

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

    public Long getAppServiceId() {
        return appServiceId;
    }

    public void setAppServiceId(Long appServiceId) {
        this.appServiceId = appServiceId;
    }

    public String getAppServiceName() {
        return appServiceName;
    }

    public void setAppServiceName(String appServiceName) {
        this.appServiceName = appServiceName;
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

    public Long getAppServiceProjectId() {
        return appServiceProjectId;
    }

    public void setAppServiceProjectId(Long appServiceProjectId) {
        this.appServiceProjectId = appServiceProjectId;
    }

    public DevopsServiceTargetVO getTarget() {
        return target;
    }

    public void setTarget(DevopsServiceTargetVO target) {
        this.target = target;
    }

    public DevopsServiceConfigVO getConfig() {
        return config;
    }

    public void setConfig(DevopsServiceConfigVO config) {
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

    public List<DevopsIngressVO> getDevopsIngressVOS() {
        return devopsIngressVOS;
    }

    public void setDevopsIngressVOS(List<DevopsIngressVO> devopsIngressVOS) {
        this.devopsIngressVOS = devopsIngressVOS;
    }

    public String getDns() {
        return dns;
    }

    public void setDns(String dns) {
        this.dns = dns;
    }

    public Map<String, String> getLabels() {
        return labels;
    }

    public void setLabels(Map<String, String> labels) {
        this.labels = labels;
    }
}
