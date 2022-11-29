package io.choerodon.devops.infra.enums.cd;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2020/7/3 15:11
 */
public enum PipelineTriggerTypeEnum {
    MANUAL("manual"),
    SCHEDULE("schedule"),
    APP_VERSION("app_version"),
    API("api");
    private String value;

    PipelineTriggerTypeEnum(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }
}
