package io.choerodon.devops.api.vo;

import io.swagger.annotations.ApiModelProperty;

public class EnvSyncStatusVO {
    @ApiModelProperty("devops解析的commit sha")
    private String devopsSyncCommit;
    @ApiModelProperty("agent解析的commit sha")
    private String agentSyncCommit;
    @ApiModelProperty("saga解析的commit sha")
    private String sagaSyncCommit;
    @ApiModelProperty("commit url")
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
