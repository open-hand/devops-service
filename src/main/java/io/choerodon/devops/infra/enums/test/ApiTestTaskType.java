package io.choerodon.devops.infra.enums.test;

/**
 * 测试任务类型
 */
public enum ApiTestTaskType {
    /**
     * 任务
     */
    TASK("task"),
    /**
     * 套件
     */
    SUITE("suite");

    private final String value;

    ApiTestTaskType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
