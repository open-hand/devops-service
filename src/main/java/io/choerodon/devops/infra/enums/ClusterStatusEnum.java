package io.choerodon.devops.infra.enums;

/**
 * @author lihao
 */

public enum ClusterStatusEnum {
    /**
     * 集群创建中
     */
    OPERATING("operating"),
    /**
     * 集群未连接
     */
    UNCONNECTED("unconnected"),
    /**
     * 已连接
     */
    CONNECTED("connected"),
    /**
     * 创建失败
     */
    FAILED("failed");

    private final String status;

    ClusterStatusEnum(String status) {
        this.status = status;
    }

    public String value() {
        return this.status;
    }

}
