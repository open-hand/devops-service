package io.choerodon.devops.infra.enums;

/**
 * 命令状态
 */
public enum CommandStatus {

    /**
     * 成功
     */
    SUCCESS("success"),
    /**
     * 失败
     */
    FAILED("failed"),
    /**
     * 操作中
     */
    OPERATING("operating");

    private String status;

    CommandStatus(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }
}
