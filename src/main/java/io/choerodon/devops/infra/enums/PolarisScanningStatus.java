package io.choerodon.devops.infra.enums;

/**
 * polaris扫描的纪录状态
 *
 * @author zmf
 * @since 2/17/20
 */
public enum PolarisScanningStatus {
    OPERATING("operating"),
    TIMEOUT("timeout"),
    FINISHED("finished");

    private String status;

    PolarisScanningStatus(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }
}
