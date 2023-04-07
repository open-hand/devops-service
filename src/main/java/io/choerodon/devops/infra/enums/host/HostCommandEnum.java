package io.choerodon.devops.infra.enums.host;

/**
 * 〈功能简述〉
 * 〈〉
 * DeployDocker    = "deploy_docker"
 * RestartDocker   = "restart_docker"
 * StopDocker      = "stop_docker"
 * KillDocker      = "exit_docker"
 * DeployJar       = "deploy_jar"
 * KillJar         = "kill_jar"
 * InitAgent       = "init_agent"
 * InitAgentFailed = "init_agent_failed"
 *
 * @author wanghao
 * @Date 2021/6/28 9:08
 */
public enum HostCommandEnum {

    DEPLOY_MIDDLEWARE("deploy_middleware"),

    DEPLOY_DOCKER_COMPOSE("deploy_docker_compose"),
    KILL_DOCKER_COMPOSE("kill_docker_compose"),
    START_DOCKER_IN_COMPOSE("start_docker_in_compose"),
    RESTART_DOCKER_IN_COMPOSE("restart_docker_in_compose"),
    STOP_DOCKER_IN_COMPOSE("stop_docker_in_compose"),
    REMOVE_DOCKER_IN_COMPOSE("remove_docker_in_compose"),

    RESTART_DOCKER_COMPOSE("restart_docker_compose"),
    DEPLOY_DOCKER("deploy_docker"),
    RESTART_DOCKER("restart_docker"),
    START_DOCKER("start_docker"),
    STOP_DOCKER("stop_docker"),
    REMOVE_DOCKER("remove_docker"),
    OPERATE_INSTANCE("operate_instance"),
    UPDATE_PROB_COMMAND("update_prob_command"),
    KILL_INSTANCE("kill_instance"),

    KILL_MIDDLEWARE("kill_middleware"),
    INIT_AGENT("init_agent"),
    UPGRADE_AGENT("upgrade_agent"),
    EXIT_AGENT("exit_agent"),
    INIT_AGENT_FAILED("init_agent_failed"),
    HOST_AGENT_LOG("host_agent_log"),
    HOST_AGENT_DOWNLOAD_LOG("host_agent_download_log"),
    /**
     * 该事件返回command结果
     */
    SYNC_OPERATING_COMMAND_STATUS("sync_operating_command_status"),
    ;

    private String value;

    HostCommandEnum(String value) {
        this.value = value;
    }

    public String value() {
        return this.value;
    }
}
