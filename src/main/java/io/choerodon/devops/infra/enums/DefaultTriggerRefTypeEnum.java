package io.choerodon.devops.infra.enums;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @Date 2020/4/3 16:35
 */
public enum DefaultTriggerRefTypeEnum {
    BRANCHES("branches"),
    TAGS("tags"),
    MASTER("master");

    private String value;

    DefaultTriggerRefTypeEnum(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }
    public static boolean contains(String s) {
        for (DefaultTriggerRefTypeEnum defaultTriggerRefTypeEnum : DefaultTriggerRefTypeEnum.values()) {
            if (defaultTriggerRefTypeEnum.value().equals(s)) {
                return true;
            }
        }
        return false;
    }
}

