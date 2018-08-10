package io.choerodon.devops.api.dto;

public class DevopsEnvFileDTO {

    private Long id;
    private Long envId;
    private String filePath;
    private String devopsCommit;
    private String agentCommit;
    private String commitUrl;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getEnvId() {
        return envId;
    }

    public void setEnvId(Long envId) {
        this.envId = envId;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getDevopsCommit() {
        return devopsCommit;
    }

    public void setDevopsCommit(String devopsCommit) {
        this.devopsCommit = devopsCommit;
    }

    public String getAgentCommit() {
        return agentCommit;
    }

    public void setAgentCommit(String agentCommit) {
        this.agentCommit = agentCommit;
    }

    public String getCommitUrl() {
        return commitUrl;
    }

    public void setCommitUrl(String commitUrl) {
        this.commitUrl = commitUrl;
    }
}
