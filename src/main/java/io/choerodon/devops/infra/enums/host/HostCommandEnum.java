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
    DEPLOY_DOCKER("deploy_docker"),
    RESTART_DOCKER("restart_docker"),
    START_DOCKER("start_docker"),
    STOP_DOCKER("stop_docker"),
    REMOVE_DOCKER("remove_docker"),
    DEPLOY_JAR("deploy_jar"),
    KILL_JAR("kill_jar"),
    INIT_AGENT("init_agent"),
    INIT_AGENT_FAILED("init_agent_failed");

    private String value;

    HostCommandEnum(String value) {
        this.value = value;
    }

    public String value() {
        return this.value;
    }
}
