package io.choerodon.devops.infra.enums;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2021/12/24 10:28
 */
public enum CiUnitTestTypeEnum {
    /**
     * Maven单元测试
     */
    MAVEN_UNIT_TEST("maven_unit_test"),
    /**
     * Go单元测试
     */
    GO_UNIT_TEST("go_unit_test"),
    /**
     * NodeJs单元测试
     */
    NODE_JS_UNIT_TEST("node_js_unit_test");

    private String value;

    CiUnitTestTypeEnum(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }
}
