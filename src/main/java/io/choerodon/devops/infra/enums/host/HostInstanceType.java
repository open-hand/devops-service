package io.choerodon.devops.infra.enums.host;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @Date 2021/6/28 14:47
 */
public enum HostInstanceType {
    /**
     * 非容器进程
     */
    NORMAL_PROCESS("normal_process"),
    /**
     * docker进程
     */
    DOCKER_PROCESS("docker_process");

    private final String value;

    HostInstanceType(String value) {
        this.value = value;
    }

    public String value() {
        return this.value;
    }
}
