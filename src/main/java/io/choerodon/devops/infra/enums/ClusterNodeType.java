package io.choerodon.devops.infra.enums;

/**
 * 节点类型
 */
public enum ClusterNodeType {
    /**
     * 主节点
     */
    MASTER("master"),
    /**
     * 工作节点
     */
    WORKER("worker"),
    /**
     * etcd节点
     */
    ETCD("etcd");

    private final String type;

    ClusterNodeType(String type) {
        this.type = type;
    }

    public String getType() {
        return this.type;
    }
}
