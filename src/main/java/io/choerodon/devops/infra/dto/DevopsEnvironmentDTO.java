package io.choerodon.devops.infra.dto;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import io.choerodon.mybatis.entity.BaseDTO;

/**
 * Created by younger on 2018/4/9.
 */
@Table(name = "devops_env")
public class DevopsEnvironmentDTO extends BaseDTO {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long projectId;
    private Long clusterId;
    private Long gitlabEnvProjectId;
    private Long hookId;
    private String envIdRsa;
    private String envIdRsaPub;
    private String name;
    private String code;
    private String token;
    private Long sequence;
    private String description;
    private Boolean isActive;
    private Long devopsEnvGroupId;
    private Long sagaSyncCommit;
    private Long devopsSyncCommit;
    private Long agentSyncCommit;
    private Boolean isSynchro;
    private Boolean isFailed;

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

    public Long getDevopsEnvGroupId() {
        return devopsEnvGroupId;
    }

    public void setDevopsEnvGroupId(Long devopsEnvGroupId) {
        this.devopsEnvGroupId = devopsEnvGroupId;
    }

    public Long getClusterId() {
        return clusterId;
    }

    public void setClusterId(Long clusterId) {
        this.clusterId = clusterId;
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
}
