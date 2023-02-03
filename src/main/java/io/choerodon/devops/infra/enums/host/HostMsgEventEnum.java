package io.choerodon.devops.infra.enums.host;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @Date 2021/6/25 15:10
 */
public enum HostMsgEventEnum {

    /**
     * 资源使用情况更新
     */
    RESOURCE_USAGE_INFO_UPDATE("resource_usage_info_update"),
    /**
     * 实例进程更新
     */
    INSTANCE_PROCESS_UPDATE("instance_process_update"),

    /**
     * docker_compose应用下容器进程更新
     */
    DOCKER_COMPOSE_PROCESS_UPDATE("docker_compose_process_update"),
    /**
     * docker进程更新
     */
    DOCKER_PROCESS_UPDATE("docker_process_update"),
    /**
     * 该事件用于告知devops-service将超过3分钟仍处于操作状态的事件发送给agent
     */
    SYNC_OPERATING_COMMAND_STATUS_EVENT("sync_operating_command_status_event"),
    /**
     * 该事件返回command结果
     */
    SYNC_OPERATING_COMMAND_STATUS("sync_operating_command_status"),
    /**
     * 更新长时间状态未更新的记录状态
     */
    SYNC_COMMAND_STATUS("sync_command_status"),
    /**
     * 初始化
     */
    INIT_AGENT("init_agent");

    private final String value;

    HostMsgEventEnum(String value) {
        this.value = value;
    }

    public String value() {
        return this.value;
    }
}
