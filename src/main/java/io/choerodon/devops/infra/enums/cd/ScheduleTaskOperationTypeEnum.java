package io.choerodon.devops.infra.enums.cd;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2021/12/15 16:04
 */
public enum ScheduleTaskOperationTypeEnum {

    CREATE("create"),
    UPDATE("update"),
    DELETE("delete");

    private final String value;

    ScheduleTaskOperationTypeEnum(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }

}
