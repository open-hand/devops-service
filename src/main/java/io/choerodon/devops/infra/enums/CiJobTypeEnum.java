package io.choerodon.devops.infra.enums;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2020/4/3 16:57
 */
public enum CiJobTypeEnum {
    /**
     * 构建
     */
    BUILD("build"),
    /**
     * maven sonar检查
     */
    SONAR("sonar"),
    /**
     * maven chart 类型
     */
    CHART("chart"),
    /**
     * 自定义任务
     */
    CUSTOM("custom");

    private final String value;

    CiJobTypeEnum(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }
}
