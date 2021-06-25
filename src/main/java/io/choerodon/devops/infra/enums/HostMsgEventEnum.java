package io.choerodon.devops.infra.enums;

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
     * java进程更新
     */
    JAVA_PROCESS_UPDATE("java_process_update"),
    /**
     * docker进程更新
     */
    DOCKER_PROCESS_UPDATE("docker_process_update"),
    /**
     * 更新事件状态
     */
    SYNC_COMMAND_STATUS("sync_command_status"),
    /**
     * agent启动初始化
     */
    INIT("init");

    private final String value;

    HostMsgEventEnum(String value) {
        this.value = value;
    }

    public String value() {
        return this.value;
    }
}
