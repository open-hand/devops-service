package io.choerodon.devops.api.vo;

/**
 * User: Mr.Wang
 * Date: 2020/2/24
 */
public class ClusterOverViewVO {
    //组织下已连接集群的数量
    private Integer connectedClusters;
    //组织下未连接集群的数量
    private Integer unconnectedCluster;

    public ClusterOverViewVO() {
    }

    public ClusterOverViewVO(Integer connectedClusters, Integer unconnectedCluster) {
        this.connectedClusters = connectedClusters;
        this.unconnectedCluster = unconnectedCluster;
    }

    public Integer getConnectedClusters() {
        return connectedClusters;
    }

    public void setConnectedClusters(Integer connectedClusters) {
        this.connectedClusters = connectedClusters;
    }

    public Integer getUnconnectedCluster() {
        return unconnectedCluster;
    }

    public void setUnconnectedCluster(Integer unconnectedCluster) {
        this.unconnectedCluster = unconnectedCluster;
    }
}
