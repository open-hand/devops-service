package io.choerodon.devops.infra.enums;

/**
 * 环境的类型
 *
 * @author zmf
 * @since 10/28/19
 */
public enum EnvironmentType {
    /**
     * 集群对应的环境类型
     */
    SYSTEM("system"),
    /**
     * 用户创建的环境类型
     */
    USER("user");

    private String value;

    EnvironmentType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static EnvironmentType forValue(String value) {
        return EnvironmentType.valueOf(value.toUpperCase());
    }

    @Override
    public String toString() {
        return value;
    }
}
