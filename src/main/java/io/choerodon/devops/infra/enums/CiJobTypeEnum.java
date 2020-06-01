package io.choerodon.devops.infra.enums;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2020/4/3 16:57
 */
public enum CiJobTypeEnum {
    BUILD("build"),
    SONAR("sonar"),
    /**
     * maven chart 类型
     */
    CHART("chart"),
    CUSTOM("custom");

    private final String value;

    CiJobTypeEnum(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }
}
