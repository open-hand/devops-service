package io.choerodon.devops.api.vo.deploy;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2022/4/7 14:31
 */
public class DockerComposeDeployDTO {
    private Long hostId;
    private Long appId;
    private String value;
    private String runCommand;

    public DockerComposeDeployDTO() {
    }

    public DockerComposeDeployDTO(Long hostId, Long appId, String value, String runCommand) {
        this.hostId = hostId;
        this.appId = appId;
        this.value = value;
        this.runCommand = runCommand;
    }

    public Long getHostId() {
        return hostId;
    }

    public void setHostId(Long hostId) {
        this.hostId = hostId;
    }

    public Long getAppId() {
        return appId;
    }

    public void setAppId(Long appId) {
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

