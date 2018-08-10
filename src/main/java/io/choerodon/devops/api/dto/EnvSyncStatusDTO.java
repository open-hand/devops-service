package io.choerodon.devops.api.dto;

public class EnvSyncStatusDTO {

    private String devopsSyncCommit;
    private String agentSyncCommit;
    private String gitCommit;
    private String commitUrl;

    public String getDevopsSyncCommit() {
        return devopsSyncCommit;
    }

    public void setDevopsSyncCommit(String devopsSyncCommit) {
        this.devopsSyncCommit = devopsSyncCommit;
    }

    public String getAgentSyncCommit() {
        return agentSyncCommit;
    }

    public void setAgentSyncCommit(String agentSyncCommit) {
        this.agentSyncCommit = agentSyncCommit;
    }

    public String getGitCommit() {
        return gitCommit;
    }

    public void setGitCommit(String gitCommit) {
        this.gitCommit = gitCommit;
    }

    public String getCommitUrl() {
        return commitUrl;
    }

    public void setCommitUrl(String commitUrl) {
        this.commitUrl = commitUrl;
    }
}
