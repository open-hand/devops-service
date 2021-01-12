package io.choerodon.devops.infra.enums.test;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2021/1/12 10:07
 */
public enum ApiTestTaskTriggerTypeEnum {

    PIPELINE("pipeline"),
    AUTO("auto"),
    MANUAL("manual");


    private String value;

    ApiTestTaskTriggerTypeEnum(String value) {
        this.value = value;
    }

    public String value() {
        return this.value;
    }
}
