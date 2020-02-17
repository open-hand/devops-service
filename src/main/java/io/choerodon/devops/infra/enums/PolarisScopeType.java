package io.choerodon.devops.infra.enums;

import javax.annotation.Nullable;

/**
 * Polaris扫描范围
 *
 * @author zmf
 * @since 2/17/20
 */
public enum PolarisScopeType {
    ENV("env"), CLUSTER("cluster");

    private String value;

    PolarisScopeType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    /**
     * 根据字符串值获取枚举类型
     *
     * @param value 值
     * @return 对应的枚举值
     */
    @Nullable
    public static PolarisScopeType forValue(String value) {
        if (ENV.getValue().equals(value)) {
            return ENV;
        } else if (CLUSTER.getValue().equals(value)) {
            return CLUSTER;
        } else {
            return null;
        }
    }
}
