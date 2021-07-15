package io.choerodon.devops.infra.enums;

/**
 * @author shanyu
 * @date 2021/7/15
 * @description
 */
public enum HostConnectionType {
    /**
     * 自动连接
     */
    AUTOMATIC("automatic"),
    /**
     * 手动连接
     */
    MANUAL("manual");

    private String value;

    HostConnectionType(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }
}
