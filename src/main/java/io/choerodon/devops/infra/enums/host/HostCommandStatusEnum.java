package io.choerodon.devops.infra.enums.host;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @Date 2021/6/28 14:50
 */
public enum HostCommandStatusEnum {
    /**
     * 成功
     */
    SUCCESS("success"),
    /**
     * 失败
     */
    FAILED("failed"),
    /**
     * 操作中
     */
    OPERATING("operating");

    private final String value;

    HostCommandStatusEnum(String value) {
        this.value = value;
    }

    public String value() {
        return this.value;
    }
}
