package io.choerodon.devops.infra.enums.test;

/**
 * 测试任务的执行触发类型
 *
 * @author zmf
 * @since 2021/1/12
 */
public enum ApiTestTriggerType {
    /**
     * 定时
     */
    TIMING("timing"),
    /**
     * 手动
     */
    MANUAL("manual"),
    /**
     * 流水线
     */
    PIPELINE("pipeline");

    private final String value;

    ApiTestTriggerType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
