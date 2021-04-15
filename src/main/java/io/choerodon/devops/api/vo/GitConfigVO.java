package io.choerodon.devops.api.vo;

import java.util.List;

import io.swagger.annotations.ApiModelProperty;

public class GitConfigVO {
    private String gitHost;
    private List<GitEnvConfigVO> envs;
    @ApiModelProperty("Agent的Helm Release code")
    private String agentName;
    @ApiModelProperty("集群已经安装的CertManager的版本/未安装的情况下就是空")
    private String certManagerVersion;
    @ApiModelProperty("Agent初始化repo时并发数")
    private Integer repoConcurrencySyncSize;

    public String getGitHost() {
        return gitHost;
    }

    public void setGitHost(String gitHost) {
        this.gitHost = gitHost;
    }

    public List<GitEnvConfigVO> getEnvs() {
        return envs;
    }

    public void setEnvs(List<GitEnvConfigVO> envs) {
        this.envs = envs;
    }

    public String getAgentName() {
        return agentName;
    }

    public void setAgentName(String agentName) {
        this.agentName = agentName;
    }

    public String getCertManagerVersion() {
        return certManagerVersion;
    }

    public void setCertManagerVersion(String certManagerVersion) {
        this.certManagerVersion = certManagerVersion;
    }

    public Integer getRepoConcurrencySyncSize() {
        return repoConcurrencySyncSize;
    }

    public void setRepoConcurrencySyncSize(Integer repoConcurrencySyncSize) {
        this.repoConcurrencySyncSize = repoConcurrencySyncSize;
    }
}
