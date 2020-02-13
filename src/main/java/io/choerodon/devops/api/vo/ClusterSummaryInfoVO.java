package io.choerodon.devops.api.vo;

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
public class ClusterSummaryInfoVO {
    @ApiModelProperty("Kubernetes版本")
    private String kubernetesVersion;
    @ApiModelProperty("集群的pod数量")
    private Long podCount;
    @ApiModelProperty("集群的namespace数量")
    private Long namespaceCount;
    @ApiModelProperty("集群节点的数量")
    private Long nodeCount;

    public String getKubernetesVersion() {
        return kubernetesVersion;
    }

    public void setKubernetesVersion(String kubernetesVersion) {
        this.kubernetesVersion = kubernetesVersion;
    }

    public Long getPodCount() {
        return podCount;
    }

    public void setPodCount(Long podCount) {
        this.podCount = podCount;
    }

    public Long getNamespaceCount() {
        return namespaceCount;
    }

    public void setNamespaceCount(Long namespaceCount) {
        this.namespaceCount = namespaceCount;
    }

    public Long getNodeCount() {
        return nodeCount;
    }

    public void setNodeCount(Long nodeCount) {
        this.nodeCount = nodeCount;
    }
}
