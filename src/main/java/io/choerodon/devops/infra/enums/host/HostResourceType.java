package io.choerodon.devops.infra.enums.host;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @Date 2021/6/28 14:47
 */
public enum HostResourceType {
    /**
     * instance进程
     */
    INSTANCE_PROCESS("instance_process"),
    /**
     * docker_compose进程
     */
    DOCKER_COMPOSE("docker_compose"),
    /**
     * docker进程
     */
    DOCKER_PROCESS("docker_process");

    private final String value;

    HostResourceType(String value) {
        this.value = value;
    }

    public String value() {
        return this.value;
    }
}
