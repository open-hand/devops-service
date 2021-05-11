package io.choerodon.devops.infra.enums;

/**
 * 主机的类型
 *
 * @author zmf
 * @since 2020/9/14
 */
public enum DevopsHostType {
    /**
     * 部署类型
     */
    DEPLOY("deploy"),
    /**
     * 分布式测试类型
     */
    @Deprecated
    DISTRIBUTE_TEST("distribute_test");

    private final String value;

    DevopsHostType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
