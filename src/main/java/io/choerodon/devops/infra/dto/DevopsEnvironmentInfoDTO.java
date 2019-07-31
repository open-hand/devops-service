package io.choerodon.devops.infra.dto;

/**
 * 展示环境信息及一些相关的信息
 *
 * @author zmf
 */
public class DevopsEnvironmentInfoDTO {
    private Long id;
    private String code;
    private String name;
    private Boolean synchronize;
    private Long clusterId;
    private String clusterName;
    private Long sagaSyncCommit;
    private Long devopsSyncCommit;
    private Long agentSyncCommit;
    private Boolean isSkipCheckPermission;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Boolean getSynchronize() {
        return synchronize;
    }

    public void setSynchronize(Boolean synchronize) {
        this.synchronize = synchronize;
    }

    public Long getClusterId() {
        return clusterId;
    }

    public void setClusterId(Long clusterId) {
        this.clusterId = clusterId;
    }

    public String getClusterName() {
        return clusterName;
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }

    public Long getSagaSyncCommit() {
        return sagaSyncCommit;
    }

    public void setSagaSyncCommit(Long sagaSyncCommit) {
        this.sagaSyncCommit = sagaSyncCommit;
    }

    public Long getDevopsSyncCommit() {
        return devopsSyncCommit;
    }

    public void setDevopsSyncCommit(Long devopsSyncCommit) {
        this.devopsSyncCommit = devopsSyncCommit;
    }

    public Long getAgentSyncCommit() {
        return agentSyncCommit;
    }

    public void setAgentSyncCommit(Long agentSyncCommit) {
        this.agentSyncCommit = agentSyncCommit;
    }

    public Boolean getSkipCheckPermission() {
        return isSkipCheckPermission;
    }

    public void setSkipCheckPermission(Boolean skipCheckPermission) {
        isSkipCheckPermission = skipCheckPermission;
    }
}
