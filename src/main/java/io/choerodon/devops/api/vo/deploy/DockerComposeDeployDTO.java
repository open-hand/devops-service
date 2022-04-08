package io.choerodon.devops.api.vo.deploy;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2022/4/7 14:31
 */
public class DockerComposeDeployDTO {
    private String hostId;
    private String appId;
    private String value;
    private String runCommand;

    public DockerComposeDeployDTO() {
    }

    public DockerComposeDeployDTO(String hostId, String appId, String value, String runCommand) {
        this.hostId = hostId;
        this.appId = appId;
        this.value = value;
        this.runCommand = runCommand;
    }

    public String getHostId() {
        return hostId;
    }

    public void setHostId(String hostId) {
        this.hostId = hostId;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getRunCommand() {
        return runCommand;
    }

    public void setRunCommand(String runCommand) {
        this.runCommand = runCommand;
    }
}

