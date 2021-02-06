package io.choerodon.devops.infra.enums;

/**
 * 集群类型
 */
public enum ClusterTypeEnum {
    /**
     * 创建类型
     */
    CREATED("created"),
    /**
     * 导入类型
     */
    IMPORTED("imported");

    private final String type;

    ClusterTypeEnum(String type) {
        this.type = type;
    }

    public String value() {
        return this.type;
    }
}
