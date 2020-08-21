package io.choerodon.devops.infra.enums;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2020/7/3 15:03
 */
public enum TriggerTypeEnum {
    AUTO("auto"),
    MANUAL("manual");

    private String value;

    TriggerTypeEnum(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }
}
