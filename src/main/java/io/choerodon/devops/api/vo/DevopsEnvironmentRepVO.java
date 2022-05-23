package io.choerodon.devops.api.vo;


import io.swagger.annotations.ApiModelProperty;
import org.hzero.starter.keyencrypt.core.Encrypt;

/**
 * Created by younger on 2018/4/9.
 */
public class DevopsEnvironmentRepVO {
    @Encrypt
    private Long id;
    @ApiModelProperty("环境名称")
    private String name;
    @ApiModelProperty("环境描述")
    private String description;
    @ApiModelProperty("环境编码")
    private String code;
    @ApiModelProperty("环境是否启用")
    private Boolean isActive;
    @ApiModelProperty("环境是否连接")
    private Boolean isConnected;
    @ApiModelProperty("环境gitops库 project id")
    private Long gitlabEnvProjectId;
    @ApiModelProperty("环境所属集群名称")
    private String clusterName;
    @Encrypt
    @ApiModelProperty("环境所属集群id")
    private Long clusterId;
    @Encrypt
    @ApiModelProperty("环境所属分组id")
    private Long devopsEnvGroupId;
    @ApiModelProperty("是否拥有环境权限")
    private Boolean permission;
    @ApiModelProperty("是否同步")
    private Boolean isSynchro;
    @ApiModelProperty("是否失败")
    private Boolean isFailed;
    @ApiModelProperty("是否跳过环境权限校验")
    private Boolean skipCheckPermission;
    private Long objectVersionNumber;
    @Encrypt
    @ApiModelProperty("环境关联的saga实例id")
    private Long sagaInstanceId;

    public Long getSagaInstanceId() {
        return sagaInstanceId;
    }

    public void setSagaInstanceId(Long sagaInstanceId) {
        this.sagaInstanceId = sagaInstanceId;
    }

    public Long getObjectVersionNumber() {
        return objectVersionNumber;
    }

    public void setObjectVersionNumber(Long objectVersionNumber) {
        this.objectVersionNumber = objectVersionNumber;
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

    public Boolean getConnected() {
        return isConnected;
    }

    public void setConnected(Boolean connected) {
        isConnected = connected;
    }

    public Boolean getSkipCheckPermission() {
        return skipCheckPermission;
    }

    public void setSkipCheckPermission(Boolean skipCheckPermission) {
        this.skipCheckPermission = skipCheckPermission;
    }
}
