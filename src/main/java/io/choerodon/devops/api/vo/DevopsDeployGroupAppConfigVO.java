package io.choerodon.devops.api.vo;

import java.util.Map;

import io.swagger.annotations.ApiModelProperty;

public class DevopsDeployGroupAppConfigVO {
    @ApiModelProperty(name = "labels")
    private Map<String, String> labels;

    @ApiModelProperty(name = "annotations")
    private Map<String, String> annotations;

    @ApiModelProperty(name = "pod总数")
    private Integer replicas;

    @ApiModelProperty(name = "升级过程中，允许【超出副本数量】的最大数值")
    private String maxSurge;
    @ApiModelProperty(name = "升级过程中，不可用实例的最大数量")
    private String maxUnavailable;

    @ApiModelProperty(name = "dns策略")
    private String dnsPolicy;

    @ApiModelProperty(name = "容器解析域名时查询的 DNS 服务器的 IP 地址列表。最多可以指定 3 个 IP 地址。逗号分隔")
    private String nameServers;

    @ApiModelProperty(name = "定义域名的搜索域列表。可选，Kubernetes 最多允许 6 个搜索域。逗号分隔")
    private String searches;

    @ApiModelProperty(name = "定义域名解析配置文件的其他选项，常见的有 timeout、attempts 和 ndots 等等。")
    private Map<String, String> options;

    @ApiModelProperty(name = "node选择器")
    private Map<String, String> nodeSelector;

    @ApiModelProperty(name = "hostalias")
    private Map<String, String> hostAlias;

    public Map<String, String> getHostAlias() {
        return hostAlias;
    }

    public void setHostAlias(Map<String, String> hostAlias) {
        this.hostAlias = hostAlias;
    }

    public Map<String, String> getNodeSelector() {
        return nodeSelector;
    }

    public void setNodeSelector(Map<String, String> nodeSelector) {
        this.nodeSelector = nodeSelector;
    }

    public String getNameServers() {
        return nameServers;
    }

    public void setNameServers(String nameServers) {
        this.nameServers = nameServers;
    }

    public String getSearches() {
        return searches;
    }

    public void setSearches(String searches) {
        this.searches = searches;
    }

    public Map<String, String> getOptions() {
        return options;
    }

    public void setOptions(Map<String, String> options) {
        this.options = options;
    }

    public String getDnsPolicy() {
        return dnsPolicy;
    }

    public void setDnsPolicy(String dnsPolicy) {
        this.dnsPolicy = dnsPolicy;
    }

    public String getMaxSurge() {
        return maxSurge;
    }

    public void setMaxSurge(String maxSurge) {
        this.maxSurge = maxSurge;
    }

    public String getMaxUnavailable() {
        return maxUnavailable;
    }

    public void setMaxUnavailable(String maxUnavailable) {
        this.maxUnavailable = maxUnavailable;
    }

    public Integer getReplicas() {
        return replicas;
    }

    public void setReplicas(Integer replicas) {
        this.replicas = replicas;
    }

    public Map<String, String> getLabels() {
        return labels;
    }

    public void setLabels(Map<String, String> labels) {
        this.labels = labels;
    }

    public Map<String, String> getAnnotations() {
        return annotations;
    }

    public void setAnnotations(Map<String, String> annotations) {
        this.annotations = annotations;
    }
}
