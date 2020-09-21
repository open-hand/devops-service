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
     * 占用中 (测试类型的主机执行分布式测试时会处于这个状态)
     */
    OCCUPIED("occupied"),
    /**
     * 测试中
     */
    OPERATING("operating");

    private final String value;

    DevopsHostStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
