package io.choerodon.devops.infra.enums;

/**
 * 节点类型枚举
 */
public enum ClusterNodeTypeEnum {
    /**
     * 作为集群节点类型
     */
    INNER("inner"),
    /**
     * 提供外网访问地址类型
     */
    OUTTER("outter");

    private String type;

    ClusterNodeTypeEnum(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }
}
