package io.choerodon.devops.infra.enums.deploy;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2021/7/30 9:30
 */
public enum DeployResultEnum {

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
    OPERATING("operating"),
    /**
     * 已取消
     */
    CANCELED("canceled");

    private String value;

    DeployResultEnum(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }
}
