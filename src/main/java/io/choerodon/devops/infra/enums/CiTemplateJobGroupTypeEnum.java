package io.choerodon.devops.infra.enums;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2021/12/22 16:49
 */
public enum CiTemplateJobGroupTypeEnum {
    /**
     * 构建
     */
    BUILD("build"),
    /**
     * docker构建
     */
    DOCKER_BUILD("docker-build"),

    /**
     * 测试构建
     */
    TEST_BUILD("test-build"),

    /**
     * 单元测试
     */
    UNIT_TEST("unit-test"),
    /**
     * 代码扫描
     */
    CODE_SCAN("code-scan"),
    /**
     * 其他
     */
    OTHER("other");

    private final String value;

    CiTemplateJobGroupTypeEnum(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }
}
