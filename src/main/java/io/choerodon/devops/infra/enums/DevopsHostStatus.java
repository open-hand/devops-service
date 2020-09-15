package io.choerodon.devops.infra.enums;

/**
 * 主机测试的状态, 成功/失败/测试中
 *
 * @author zmf
 * @since 2020/9/15
 */
public enum DevopsHostStatus {
    /**
     * 成功
     */
    SUCCESS("success"),
    /**
     * 失败
     */
    FAILED("failed"),
    /**
     * 测试中
     */
    OPERATING("operating");

    private final String values;

    DevopsHostStatus(String values) {
        this.values = values;
    }

    public String getValues() {
        return values;
    }
}
