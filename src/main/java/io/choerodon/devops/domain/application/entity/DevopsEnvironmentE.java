package io.choerodon.devops.domain.application.entity;

import java.util.List;
import java.util.LongSummaryStatistics;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * Created by younger on 2018/4/9.
 */

@Component
@Scope("prototype")
public class DevopsEnvironmentE {
    private Long id;
    private ProjectE projectE;
    private DevopsClusterE clusterE;
    private Long gitlabEnvProjectId;
    private Long devopsEnvGroupId;
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
    private Boolean permission;

    private Long devopsSyncCommit;
    private Long agentSyncCommit;
    private Long sagaSyncCommit;

    /**
     * 重写构造方法
     *
     * @param id          环境Id
     * @param name        环境name
     * @param description 环境描述
     * @param isConnect   环境是否连接
     * @param isActive    环境是否启用
     * @param code        环境code
     */
    public DevopsEnvironmentE(Long id, String name, String description, Boolean isConnect, Boolean isActive, String code) {

        this.id = id;
        this.name = name;
        this.description = description;
        this.isConnected = isConnect;
        this.isActive = isActive;
        this.code = code;
    }

    /**
     * 构造函数
     *
     * @param id   环境Id
     * @param code 环境code
     * @param name 环境name
     */
    public DevopsEnvironmentE(Long id, String code, String name) {
        this.id = id;
        this.code = code;
        this.name = name;
    }


    public DevopsEnvironmentE() {
    }

    public DevopsEnvironmentE(Long id) {
        this.id = id;
    }

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

    public ProjectE getProjectE() {
        return projectE;
    }

    public void setProjectE(ProjectE projectE) {
        this.projectE = projectE;
    }

    public void initProjectE(Long id) {
        this.projectE = new ProjectE(id);
    }

    public void initActive(Boolean active) {
        this.isActive = active;
    }

    public void initConnect(Boolean connect) {
        this.isConnected = connect;

    }

    public Long getSequence() {
        return sequence;
    }

    public void setSequence(Long sequence) {
        this.sequence = sequence;
    }

    public DevopsEnvironmentE resetOrder(Long sequence) {
        this.sequence = sequence;
        return this;
    }

    public void initToken(String token) {
        this.token = token;
    }

    public Long getGitlabEnvProjectId() {
        return gitlabEnvProjectId;
    }

    public void setGitlabEnvProjectId(Long gitlabEnvProjectId) {
        this.gitlabEnvProjectId = gitlabEnvProjectId;
    }

    public void initGitlabEnvProjectId(Long gitlabEnvProjectId) {
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

    public Boolean getConnect() {
        return isConnected;
    }

    public void setConnect(Boolean connect) {
        isConnected = connect;
    }

    public Long getHookId() {
        return hookId;
    }

    public void setHookId(Long hookId) {
        this.hookId = hookId;
    }

    public void initHookId(Long hookId) {
        this.hookId = hookId;
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

    public Boolean getConnected() {
        return isConnected;
    }

    public void setConnected(Boolean connected) {
        isConnected = connected;
    }

    public Long getSagaSyncCommit() {
        return sagaSyncCommit;
    }

    public void setSagaSyncCommit(Long sagaSyncCommit) {
        this.sagaSyncCommit = sagaSyncCommit;
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

    public DevopsClusterE getClusterE() {
        return clusterE;
    }

    public void initDevopsClusterEById(Long id) {
        this.clusterE = new DevopsClusterE(id);
    }

    /**
     * 初始化序列
     */
    public void initSequence(List<DevopsEnvironmentE> devopsEnvironmentES) {
        this.sequence = 1L;
        if (!devopsEnvironmentES.isEmpty()) {
            LongSummaryStatistics stats = devopsEnvironmentES
                    .stream()
                    .mapToLong(DevopsEnvironmentE::getSequence)
                    .summaryStatistics();
            this.sequence = stats.getMax() + 1;
        }
    }
}
