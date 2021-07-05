package io.choerodon.devops.infra.enums.deploy;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @Date 2021/7/5 10:23
 */
public enum JavaInstanceStatusEnum {
    /**
     * 操作中
     */
    OPERATING("operating"),
    /**
     * 运行中
     */
    RUNNING("running"),
    /**
     * 已移除
     */
    REMOVED("removed");

    private final String value;

    JavaInstanceStatusEnum(String value) {
        this.value = value;
    }

    public String value() {
        return this.value;
    }
}
