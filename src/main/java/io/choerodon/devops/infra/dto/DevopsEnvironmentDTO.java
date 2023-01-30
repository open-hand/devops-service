package io.choerodon.devops.infra.dto;

import java.util.List;
import javax.persistence.*;

import io.swagger.annotations.ApiModelProperty;
import org.hzero.starter.keyencrypt.core.Encrypt;

import io.choerodon.mybatis.annotation.ModifyAudit;
import io.choerodon.mybatis.annotation.VersionAudit;
import io.choerodon.mybatis.domain.AuditDomain;

/**
 * if (!isSynchro) {
 * // 处理中
 * } else {
 * if (isFailed) {
 * // 失败
 * } else {
 * // 成功
 * }
 * }
 * <p>
 * Created by younger on 2018/4/9.
 */
@ModifyAudit
@VersionAudit
@Table(name = "devops_env")
public class DevopsEnvironmentDTO extends AuditDomain {

    public static final String ENCRYPT_KEY = "devops_env";

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Encrypt
    private Long id;
    @ApiModelProperty("环境所属项目id")
    private Long projectId;
    @ApiModelProperty("环境所属集群id")
    private Long clusterId;
    @ApiModelProperty("环境对应的gitlab project id")
    private Long gitlabEnvProjectId;
    @ApiModelProperty("gitops webhook id")
    private Long hookId;
    @ApiModelProperty("环境私钥")
    private String envIdRsa;
    @ApiModelProperty("环境公钥")
    private String envIdRsaPub;
    @ApiModelProperty("环境名称")
    private String name;
    @ApiModelProperty("环境编码")
    private String code;
    @ApiModelProperty("环境token")
    private String token;

    @ApiModelProperty("环境的类型 user/system")
    private String type;
    @ApiModelProperty("环境描述")
    private String description;
    @ApiModelProperty("是否启用")
    private Boolean isActive;
    @ApiModelProperty("环境所属的分组 id")
    private Long devopsEnvGroupId;
    @ApiModelProperty("saga 同步的commit sha")
    private Long sagaSyncCommit;
    @ApiModelProperty("devops 解析的commit sha")
    private Long devopsSyncCommit;
    @ApiModelProperty("agent 解析的commit sha")
    private Long agentSyncCommit;
    @ApiModelProperty("环境是否同步")
    private Boolean isSynchro;
    @ApiModelProperty("环境是否失败")
    private Boolean isFailed;
    @ApiModelProperty("环境是否跳过权限校验")
    private Boolean isSkipCheckPermission;
    @ApiModelProperty("是否开启流水线部署")
    private Boolean isAutoDeploy;
    @ApiModelProperty("是否开启确认副本生效策略，默认为false")
    private Boolean checkValuesPolicy;
    @Transient
    @ApiModelProperty("环境是否连接")
    private Boolean connected;
    @Transient
    @ApiModelProperty("是否拥有环境权限")
    private Boolean permission;
    @Transient
    @ApiModelProperty("环境所属集群名称")
    private String clusterName;
    @Transient
    @ApiModelProperty("环境所属集群编码")
    private String clusterCode;

    @ApiModelProperty("环境下实例的code")
    @Transient
    private List<String> instances;

    public Boolean getAutoDeploy() {
        return isAutoDeploy;
    }

    public void setAutoDeploy(Boolean autoDeploy) {
        isAutoDeploy = autoDeploy;
    }

    public String getClusterName() {
        return clusterName;
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }

    public Boolean getPermission() {
        return permission;
    }

    public void setPermission(Boolean permission) {
        this.permission = permission;
    }

    public Boolean getConnected() {
        return connected;
    }

    public void setConnected(Boolean connected) {
        this.connected = connected;
    }

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

    public Boolean getSkipCheckPermission() {
        return isSkipCheckPermission;
    }

    public void setSkipCheckPermission(Boolean skipCheckPermission) {
        isSkipCheckPermission = skipCheckPermission;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getClusterCode() {
        return clusterCode;
    }

    public void setClusterCode(String clusterCode) {
        this.clusterCode = clusterCode;
    }

    public List<String> getInstances() {
        return instances;
    }

    public void setInstances(List<String> instances) {
        this.instances = instances;
    }

    public Boolean getCheckValuesPolicy() {
        return checkValuesPolicy;
    }

    public void setCheckValuesPolicy(Boolean checkValuesPolicy) {
        this.checkValuesPolicy = checkValuesPolicy;
    }

    @Override
    public String toString() {
        return "DevopsEnvironmentDTO{" +
                "id=" + id +
                ", projectId=" + projectId +
                ", clusterId=" + clusterId +
                ", gitlabEnvProjectId=" + gitlabEnvProjectId +
                ", hookId=" + hookId +
                ", envIdRsa='" + envIdRsa + '\'' +
                ", envIdRsaPub='" + envIdRsaPub + '\'' +
                ", name='" + name + '\'' +
                ", code='" + code + '\'' +
                ", token='" + token + '\'' +
                ", type='" + type + '\'' +
                ", description='" + description + '\'' +
                ", isActive=" + isActive +
                ", devopsEnvGroupId=" + devopsEnvGroupId +
                ", sagaSyncCommit=" + sagaSyncCommit +
                ", devopsSyncCommit=" + devopsSyncCommit +
                ", agentSyncCommit=" + agentSyncCommit +
                ", isSynchro=" + isSynchro +
                ", isFailed=" + isFailed +
                ", isSkipCheckPermission=" + isSkipCheckPermission +
                ", connected=" + connected +
                ", permission=" + permission +
                ", clusterName='" + clusterName + '\'' +
                ", clusterCode='" + clusterCode + '\'' +
                ", instances=" + instances +
                '}';
    }
}
