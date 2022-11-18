package io.choerodon.devops.api.vo.template;

import io.swagger.annotations.ApiModelProperty;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

public class CiTplHostDeployInfoCfgVO {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String hostDeployType;
    private String deployJson;

    private String preCommand;
    private String runCommand;
    private String postCommand;
    @ApiModelProperty("删除命令")
    private String killCommand;
    @ApiModelProperty("健康探针")
    private String healthProb;
    @ApiModelProperty("删除命令")
    private String dockerCommand;

    private String imageJobName;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getHostDeployType() {
        return hostDeployType;
    }

    public void setHostDeployType(String hostDeployType) {
        this.hostDeployType = hostDeployType;
    }

    public String getDeployJson() {
        return deployJson;
    }

    public void setDeployJson(String deployJson) {
        this.deployJson = deployJson;
    }

    public String getPreCommand() {
        return preCommand;
    }

    public void setPreCommand(String preCommand) {
        this.preCommand = preCommand;
    }

    public String getRunCommand() {
        return runCommand;
    }

    public void setRunCommand(String runCommand) {
        this.runCommand = runCommand;
    }

    public String getPostCommand() {
        return postCommand;
    }

    public void setPostCommand(String postCommand) {
        this.postCommand = postCommand;
    }

    public String getKillCommand() {
        return killCommand;
    }

    public void setKillCommand(String killCommand) {
        this.killCommand = killCommand;
    }

    public String getHealthProb() {
        return healthProb;
    }

    public void setHealthProb(String healthProb) {
        this.healthProb = healthProb;
    }

    public String getDockerCommand() {
        return dockerCommand;
    }

    public void setDockerCommand(String dockerCommand) {
        this.dockerCommand = dockerCommand;
    }

    public String getImageJobName() {
        return imageJobName;
    }

    public void setImageJobName(String imageJobName) {
        this.imageJobName = imageJobName;
    }
}
