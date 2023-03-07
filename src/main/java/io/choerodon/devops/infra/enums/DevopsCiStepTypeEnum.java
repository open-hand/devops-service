package io.choerodon.devops.infra.enums;

/**
 * 〈功能简述〉
 * 〈流水线步骤类型枚举〉
 *
 * @author wanghao
 * @since 2021/11/29 11:41
 */
public enum DevopsCiStepTypeEnum {

    // 单元测试相关
    /**
     * 通用单元测试
     */
    GENERAL_UNIT_TEST("general_unit_test"),
    /**
     * Maven单元测试
     */
    MAVEN_UNIT_TEST("maven_unit_test"),
    /**
     * Go单元测试
     */
    GO_UNIT_TEST("go_unit_test"),
    /**
     * Node.js单元测试
     */
    NODE_JS_UNIT_TEST("node_js_unit_test"),

    // 构建相关
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
     * docker构建
     */
    DOCKER_BUILD("docker_build"),

    /**
     * mvn 发布
     */
    MAVEN_PUBLISH("maven_publish"),

    /**
     * sonar检查
     */
    SONAR("sonar"),
    /**
     * 上传chart 类型
     */
    UPLOAD_CHART("upload_chart"),

    /**
     * 上传jar 类型
     */
    UPLOAD_JAR("upload_jar"),
    /**
     * Node Js构建
     */
    NODE_JS_BUILD("node_js_build"),
    /**
     * NPM 上传
     */
    NPM_UPLOAD("npm_upload"),

    /**
     * 自定义任务
     */
    CUSTOM("custom"),

    /**
     * 人工卡点任务
     */
    AUDIT("audit"),

    /**
     * 发布应用服务版本
     */
    PUBLISH_APP_VERSION("publish_app_version");


    private String value;

    DevopsCiStepTypeEnum(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }
}
