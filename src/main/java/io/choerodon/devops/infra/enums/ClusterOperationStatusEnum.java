package io.choerodon.devops.infra.enums;

/**
 * @author lihao
 * 节点检测状态
 */
public enum ClusterOperationStatusEnum {
    /**
     * 等待处理
     */
    WAIT("wait"),
    /**
     * 操作中
     */
    OPERATING("operating"),
    /**
     * 检测失败
     */
    FAILED("failed"),
    /**
     * 检测成功
     */
    SUCCESS("success");
    private final String status;

    ClusterOperationStatusEnum(String status) {
        this.status = status;
    }

    public String value() {
        return this.status;
    }
}
