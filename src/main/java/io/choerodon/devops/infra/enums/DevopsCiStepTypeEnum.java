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
     * maven 构建
     */
    MAVEN_BUILD("maven_build"),

    /**
     * npm 构建
     */
    NPM_BUILD("npm_build"),

    /**
     * go 构建
     */
    GO_BUILD("go_build"),

    /**
     * mvn 发布
     */
    MAVEN_PUBLISH("maven_publish"),
    /**
     * docker构建
     */
    DOCKER_BUILD("docker_build"),
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
