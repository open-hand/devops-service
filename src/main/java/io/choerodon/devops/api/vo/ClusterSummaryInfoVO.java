package io.choerodon.devops.api.vo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.annotations.ApiModelProperty;

/**
 * 存储集群一些信息，主要是健康检查界面需要。
 * 其中，Kubenetes版本由agent在启动时发送，devops存在redis中，
 * 其它数据如果没有进行polaris扫描就从已有的redis和数据库中统计得到，
 * 如果扫描就从返回的扫描结果中读取后存在redis中。
 *
 * @author zmf
 * @since 2/13/20
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ClusterSummaryInfoVO {
    @ApiModelProperty("Kubernetes版本")
    private String version;
    @ApiModelProperty("集群的pod数量")
    private Long pods;
    @ApiModelProperty("集群的namespace数量")
    private Long namespaces;
    @ApiModelProperty("集群节点的数量")
    private Long nodes;
    private Long deployments;
    private Long statefulSets;
    private Long daemonSets;
    private Long jobs;
    private Long cronJobs;
    private Long replicationControllers;
    private String agentPodName;
    private String agentNamespace;

    public String getAgentNamespace() {
        return agentNamespace;
    }

    public void setAgentNamespace(String agentNamespace) {
        this.agentNamespace = agentNamespace;
    }

    public String getAgentPodName() {
        return agentPodName;
    }

    public void setAgentPodName(String agentPodName) {
        this.agentPodName = agentPodName;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public Long getPods() {
        return pods;
    }

    public void setPods(Long pods) {
        this.pods = pods;
    }

    public Long getNamespaces() {
        return namespaces;
    }

    public void setNamespaces(Long namespaces) {
        this.namespaces = namespaces;
    }

    public Long getNodes() {
        return nodes;
    }

    public void setNodes(Long nodes) {
        this.nodes = nodes;
    }

    public Long getDeployments() {
        return deployments;
    }

    public void setDeployments(Long deployments) {
        this.deployments = deployments;
    }

    public Long getStatefulSets() {
        return statefulSets;
    }

    public void setStatefulSets(Long statefulSets) {
        this.statefulSets = statefulSets;
    }

    public Long getDaemonSets() {
        return daemonSets;
    }

    public void setDaemonSets(Long daemonSets) {
        this.daemonSets = daemonSets;
    }

    public Long getJobs() {
        return jobs;
    }

    public void setJobs(Long jobs) {
        this.jobs = jobs;
    }

    public Long getCronJobs() {
        return cronJobs;
    }

    public void setCronJobs(Long cronJobs) {
        this.cronJobs = cronJobs;
    }

    public Long getReplicationControllers() {
        return replicationControllers;
    }

    public void setReplicationControllers(Long replicationControllers) {
        this.replicationControllers = replicationControllers;
    }

    @Override
    public String toString() {
        return "ClusterSummaryInfoVO{" +
                "version='" + version + '\'' +
                ", pods=" + pods +
                ", namespaces=" + namespaces +
                ", nodes=" + nodes +
                ", deployments=" + deployments +
                ", statefulSets=" + statefulSets +
                ", daemonSets=" + daemonSets +
                ", jobs=" + jobs +
                ", cronJobs=" + cronJobs +
                ", replicationControllers=" + replicationControllers +
                '}';
    }
}
