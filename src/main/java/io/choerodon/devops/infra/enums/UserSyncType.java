package io.choerodon.devops.infra.enums;

/**
 * @author zmf
 * @since 2021/1/21
 */
public enum UserSyncType {
    /**
     * 手动触发的同步
     */
    MANUAL("manual"),
    /**
     * 自动触发的同步
     */
    AUTO("auto");

    private final String value;

    UserSyncType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
