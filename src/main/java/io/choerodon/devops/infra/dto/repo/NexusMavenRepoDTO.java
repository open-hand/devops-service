package io.choerodon.devops.infra.dto.repo;

import io.swagger.annotations.ApiModelProperty;

/**
 * maven仓库相关数据
 *
 * @author zmf
 * @since 2020/6/12
 */
public class NexusMavenRepoDTO {
    @ApiModelProperty(value = "主键Id")
    private Long repositoryId;
    @ApiModelProperty(value = "仓库名称")
    private String name;
    @ApiModelProperty(value = "仓库类型")
    private String type;
    @ApiModelProperty(value = "仓库url")
    private String url;
    @ApiModelProperty(value = "仓库策略")
    private String versionPolicy;

    @ApiModelProperty(value = "仓库默认发布用户Id")
    private String neUserId;
    @ApiModelProperty(value = "仓库默认发布用户密码")
    private String neUserPassword;
    @ApiModelProperty(value = "仓库默认拉取用户Id")
    private String nePullUserId;
    @ApiModelProperty(value = "仓库默认拉取用户密码")
    private String nePullUserPassword;

    public Long getRepositoryId() {
        return repositoryId;
    }

    public void setRepositoryId(Long repositoryId) {
        this.repositoryId = repositoryId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getVersionPolicy() {
        return versionPolicy;
    }

    public void setVersionPolicy(String versionPolicy) {
        this.versionPolicy = versionPolicy;
    }

    public String getNeUserId() {
        return neUserId;
    }

    public void setNeUserId(String neUserId) {
        this.neUserId = neUserId;
    }

    public String getNeUserPassword() {
        return neUserPassword;
    }

    public void setNeUserPassword(String neUserPassword) {
        this.neUserPassword = neUserPassword;
    }

    public String getNePullUserId() {
        return nePullUserId;
    }

    public void setNePullUserId(String nePullUserId) {
        this.nePullUserId = nePullUserId;
    }

    public String getNePullUserPassword() {
        return nePullUserPassword;
    }

    public void setNePullUserPassword(String nePullUserPassword) {
        this.nePullUserPassword = nePullUserPassword;
    }
}
