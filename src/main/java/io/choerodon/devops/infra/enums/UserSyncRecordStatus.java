package io.choerodon.devops.infra.enums;

/**
 * @author zmf
 * @since 2021/1/21
 */
public enum UserSyncRecordStatus {
    /**
     * 处理中
     */
    OPERATING("operating"),
    /**
     * 已结束
     */
    FINISHED("finished");

    private final String value;

    UserSyncRecordStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
