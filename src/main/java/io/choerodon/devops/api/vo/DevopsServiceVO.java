package io.choerodon.devops.api.vo;

import java.util.List;
import java.util.Map;

import io.swagger.annotations.ApiModelProperty;
import org.hzero.starter.keyencrypt.core.Encrypt;

/**
 * Created by Zenger on 2018/4/19.
 */
public class DevopsServiceVO extends DevopsResourceDataInfoVO {
    @Encrypt
    private Long id;
    @ApiModelProperty("名称")
    private String name;
    @ApiModelProperty("状态")
    private String status;
    @Encrypt
    @ApiModelProperty("环境id")
    private Long envId;
    @ApiModelProperty("环境名称")
    private String envName;
    @ApiModelProperty("环境类型")
    private String type;
    @ApiModelProperty("环境状态")
    private Boolean envStatus;
    @Encrypt
    @ApiModelProperty("应用服务id")
    private Long appServiceId;
    @Encrypt
    @ApiModelProperty("应用服务所属项目id")
    private Long appServiceProjectId;
    @ApiModelProperty("应用服务名称")
    private String appServiceName;
    @ApiModelProperty("dns")
    private String dns;
    /**
     * 网络本身的标签
     */
    @ApiModelProperty("网络本身的标签")
    private Map<String, String> labels;
    @ApiModelProperty("selectors")
    private Map<String, String> selectors;
    @ApiModelProperty("target")
    private DevopsServiceTargetVO target;
    @ApiModelProperty("网络配置")
    private DevopsServiceConfigVO config;
    @ApiModelProperty("command类型")
    private String commandType;
    @ApiModelProperty("command状态")
    private String commandStatus;
    @ApiModelProperty("错误信息")
    private String error;
    @ApiModelProperty("loadbalance ip")
    private String loadBalanceIp;
    @ApiModelProperty("绑定的igress 信息")
    private List<DevopsIngressVO> devopsIngressVOS;
    @ApiModelProperty("实例id")
    private Long instanceId;
    @ApiModelProperty("该字段包含除当前实例外，关联的其它应用实例的名称")
    private List<String> relatedApplicationName;
    /**
     * pod实时信息
     */
    @ApiModelProperty("pod实时信息")
    private List<PodLiveInfoVO> podLiveInfos;

    private Map<String, String> annotations;

    public Map<String, String> getAnnotations() {
        return annotations;
    }

    public void setAnnotations(Map<String, String> annotations) {
        this.annotations = annotations;
    }

    public Long getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(Long instanceId) {
        this.instanceId = instanceId;
    }

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

    public Map<String, String> getSelectors() {
        return selectors;
    }

    public void setSelectors(Map<String, String> selectors) {
        this.selectors = selectors;
    }

    public List<String> getRelatedApplicationName() {
        return relatedApplicationName;
    }

    public void setRelatedApplicationName(List<String> relatedApplicationName) {
        this.relatedApplicationName = relatedApplicationName;
    }
}
