package io.choerodon.devops.infra.enums;

/**
 * 操作记录对象类型
 */
public enum ClusterOperationTypeEnum {
    /**
     * 集群类型
     */
    CLUSTER("cluster"),
    /**
     * 节点类型
     */
    NODE("node");

    ClusterOperationTypeEnum(String type) {
        this.type = type;
    }

    private String type;

    public String getType() {
        return type;
    }
}
