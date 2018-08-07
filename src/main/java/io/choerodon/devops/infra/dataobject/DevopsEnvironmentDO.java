package io.choerodon.devops.infra.dataobject;

import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import io.choerodon.mybatis.annotation.ModifyAudit;
import io.choerodon.mybatis.annotation.VersionAudit;

/**
 * Created by younger on 2018/4/9.
 */
@VersionAudit
@ModifyAudit
@Table(name = "devops_env")
public class DevopsEnvironmentDO {
    @Id
    @GeneratedValue
    private Long id;
    private Long projectId;
    private Long gitlabEnvProjectId;
    private Long hookId;
    private String envIdRsa;
    private String envIdRsaPub;
    private String name;
    private String code;
    private String token;
    private Long sequence;
    private String description;
    private Boolean isConnected;
    private Boolean isActive;
    private Long gitCommit;
    private Long devopsSyncCommit;
    private Long agentSyncCommit;
    private Long objectVersionNumber;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
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

    public Long getObjectVersionNumber() {
        return objectVersionNumber;
    }

    public void setObjectVersionNumber(Long objectVersionNumber) {
        this.objectVersionNumber = objectVersionNumber;
    }

    public Long getSequence() {
        return sequence;
    }

    public void setSequence(Long sequence) {
        this.sequence = sequence;
    }

    public Long getGitlabEnvProjectId() {
        return gitlabEnvProjectId;
    }

    public void setGitlabEnvProjectId(Long gitlabEnvProjectId) {
        this.gitlabEnvProjectId = gitlabEnvProjectId;
    }

    public String getEnvIdRsa() {
        return envIdRsa;
    }

    public void setEnvIdRsa(String envIdRsa) {
        this.envIdRsa = envIdRsa;
    }

    public String getEnvIdRsaPub() {
        return envIdRsaPub;
    }

    public void setEnvIdRsaPub(String envIdRsaPub) {
        this.envIdRsaPub = envIdRsaPub;
    }

    public Long getHookId() {
        return hookId;
    }

    public void setHookId(Long hookId) {
        this.hookId = hookId;
    }

    public Long getGitCommit() {
        return gitCommit;
    }

    public void setGitCommit(Long gitCommit) {
        this.gitCommit = gitCommit;
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
}
