package io.choerodon.devops.infra.enums.deploy;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @Date 2021/6/30 14:21
 */
public enum DockerInstanceStatusEnum {
    /**
     * 运行中
     */
    RUNNING("running"),
    /**
     * 退出
     */
    EXITED("exited"),
    /**
     * 操作中
     */
    OPERATING("operating");

    private final String value;

    DockerInstanceStatusEnum(String value) {
        this.value = value;
    }

    public String value() {
        return this.value;
    }
}
