package io.choerodon.devops.infra.enums;

/**
 * @author shanyu
 * @since 2021/8/20
 */
public enum AppStatus {
    /**
     * 存在
     */
    EXIST("exist"),
    /**
     * 不存在
     */
    NOT_EXIST("notExist"),
    /**
     * 已删除
     */
    DELETED("deleted");

    private final String status;

    AppStatus(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }
}
