package io.choerodon.devops.api.dto;

public class EnvSyncStatusDTO {

    private String devopsSyncCommit;
    private String agentSyncCommit;
    private String sagaSyncCommit;
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

    public String getSagaSyncCommit() {
        return sagaSyncCommit;
    }

    public void setSagaSyncCommit(String sagaSyncCommit) {
        this.sagaSyncCommit = sagaSyncCommit;
    }

    public String getCommitUrl() {
        return commitUrl;
    }

    public void setCommitUrl(String commitUrl) {
        this.commitUrl = commitUrl;
    }
}
