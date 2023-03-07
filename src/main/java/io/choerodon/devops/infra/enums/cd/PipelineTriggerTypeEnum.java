package io.choerodon.devops.infra.enums.cd;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2020/7/3 15:11
 */
public enum PipelineTriggerTypeEnum {
    /**
     * 手动触发
     */
    MANUAL("manual"),
    /**
     * 定时触发
     */
    SCHEDULE("schedule"),
    /**
     * 应用服务版本触发
     */
    APP_VERSION("app_version"),
    /**
     * api触发
     */
    API("api");
    private String value;

    PipelineTriggerTypeEnum(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }
}
