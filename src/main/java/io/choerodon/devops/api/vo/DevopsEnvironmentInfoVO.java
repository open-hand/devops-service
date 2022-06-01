package io.choerodon.devops.api.vo;

import io.swagger.annotations.ApiModelProperty;
import org.hzero.starter.keyencrypt.core.Encrypt;

import io.choerodon.devops.infra.enums.EnvironmentGitopsStatus;

/**
 * 展示环境信息及一些相关的信息
 *
 * @author zmf
 */
public class DevopsEnvironmentInfoVO {
    @Encrypt
    private Long id;
    @ApiModelProperty("环境编码")
    private String code;
    @ApiModelProperty("环境名称")
    private String name;
    @ApiModelProperty("环境是否连接")
    private Boolean connect;
    @ApiModelProperty("环境是否同步")
    private Boolean synchronize;
    @Encrypt
    @ApiModelProperty("所属集群id")
    private Long clusterId;
    @ApiModelProperty("所属集群名称")
    private String clusterName;
    @ApiModelProperty("是否跳过权限校验")
    private Boolean skipCheckPermission;
    @ApiModelProperty("描述")
    private String description;
    @ApiModelProperty("版本号")
    private Long objectVersionNumber;
    @ApiModelProperty("是否启用")
    private Boolean active;
    @ApiModelProperty("环境是否失败")
    private Boolean fail;
    @Encrypt
    @ApiModelProperty("分组id")
    private Long groupId;
    @ApiModelProperty("gitlab url")
    private String gitlabUrl;
    /**
     * value from {@link EnvironmentGitopsStatus}
     */
    @ApiModelProperty("gitops同步状态")
    private String gitopsStatus;

    public Boolean getFail() {
        return fail;
    }

    public void setFail(Boolean fail) {
        this.fail = fail;
    }

    public Long getGroupId() {
        return groupId;
    }

    public void setGroupId(Long groupId) {
        this.groupId = groupId;
    }

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

    public Boolean getConnect() {
        return connect;
    }

    public void setConnect(Boolean connect) {
        this.connect = connect;
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

    public String getGitopsStatus() {
        return gitopsStatus;
    }

    public void setGitopsStatus(String gitopsStatus) {
        this.gitopsStatus = gitopsStatus;
    }

    public Boolean getSkipCheckPermission() {
        return skipCheckPermission;
    }

    public void setSkipCheckPermission(Boolean skipCheckPermission) {
        this.skipCheckPermission = skipCheckPermission;
    }

    public Long getObjectVersionNumber() {
        return objectVersionNumber;
    }

    public void setObjectVersionNumber(Long objectVersionNumber) {
        this.objectVersionNumber = objectVersionNumber;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public String getGitlabUrl() {
        return gitlabUrl;
    }

    public void setGitlabUrl(String gitlabUrl) {
        this.gitlabUrl = gitlabUrl;
    }
}
