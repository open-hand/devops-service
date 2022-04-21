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
    private String instanceId;
    private String value;
    private String runCommand;
    private Boolean downFlag;

    public DockerComposeDeployDTO() {
    }

    public DockerComposeDeployDTO(String hostId, String instanceId, String value, String runCommand) {
        this.hostId = hostId;
        this.instanceId = instanceId;
        this.value = value;
        this.runCommand = runCommand;
    }

    public DockerComposeDeployDTO(String hostId, String instanceId, String value, String runCommand, boolean downFlag) {
        this.hostId = hostId;
        this.instanceId = instanceId;
        this.value = value;
        this.runCommand = runCommand;
        this.downFlag = downFlag;
    }

    public Boolean getDownFlag() {
        return downFlag;
    }

    public void setDownFlag(Boolean downFlag) {
        this.downFlag = downFlag;
    }

    public String getHostId() {
        return hostId;
    }

    public void setHostId(String hostId) {
        this.hostId = hostId;
    }

    public String getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(String instanceId) {
        this.instanceId = instanceId;
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

