package io.choerodon.devops.infra.enums;

/**
 * 〈功能简述〉
 * 〈流水线步骤类型枚举〉
 *
 * @author wanghao
 * @since 2021/11/29 11:41
 */
public enum DevopsCiStepTypeEnum {


    /**
     * 构建
     */
    BUILD("build"),
    /**
     * sonar检查
     */
    SONAR("sonar"),
    /**
     * chart 类型
     */
    CHART("chart"),

    /**
     * 自定义任务
     */
    CUSTOM("custom");


    private String value;

    DevopsCiStepTypeEnum(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }
}
