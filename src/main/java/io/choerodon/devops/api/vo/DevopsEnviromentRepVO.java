package io.choerodon.devops.api.vo;


/**
 * Created by younger on 2018/4/9.
 */
public class DevopsEnviromentRepVO {
    private Long id;
    private String name;
    private String description;
    private String code;
    private Boolean isActive;
    private Boolean isConnected;
    private Long gitlabEnvProjectId;
    private String clusterName;
    private Long clusterId;
    private Long devopsEnvGroupId;
    private Boolean permission;
    private Boolean isSynchro;
    private Boolean isFailed;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean getActive() {
        return isActive;
    }

    public void setActive(Boolean active) {
        isActive = active;
    }

    public Boolean getConnect() {
        return isConnected;
    }

    public void setConnect(Boolean connect) {
        isConnected = connect;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Long getDevopsEnvGroupId() {
        return devopsEnvGroupId;
    }

    public void setDevopsEnvGroupId(Long devopsEnvGroupId) {
        this.devopsEnvGroupId = devopsEnvGroupId;
    }

    public Boolean getPermission() {
        return permission;
    }

    public void setPermission(Boolean permission) {
        this.permission = permission;
    }

    public Boolean getSynchro() {
        return isSynchro;
    }

    public void setSynchro(Boolean synchro) {
        isSynchro = synchro;
    }

    public Boolean getFailed() {
        return isFailed;
    }

    public void setFailed(Boolean failed) {
        isFailed = failed;
    }

    public String getClusterName() {
        return clusterName;
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }

    public Long getClusterId() {
        return clusterId;
    }

    public void setClusterId(Long clusterId) {
        this.clusterId = clusterId;
    }

    public Long getGitlabEnvProjectId() {
        return gitlabEnvProjectId;
    }

    public void setGitlabEnvProjectId(Long gitlabEnvProjectId) {
        this.gitlabEnvProjectId = gitlabEnvProjectId;
    }
}
