package io.choerodon.devops.infra.dto;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import io.swagger.annotations.ApiModelProperty;

import io.choerodon.mybatis.annotation.ModifyAudit;
import io.choerodon.mybatis.annotation.VersionAudit;
import io.choerodon.mybatis.domain.AuditDomain;

@Table(name = "devops_helm_config")
@ModifyAudit
@VersionAudit
public class DevopsHelmConfigDTO extends AuditDomain {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ApiModelProperty("仓库名称")
    private String name;

    @ApiModelProperty("helm仓库地址 平台层或组织层为仓库地址前缀部分 项目层是完整的仓库地址")
    private String url;

    @ApiModelProperty("仓库账号")
    private String username;

    @ApiModelProperty("仓库密码")
    private String password;

    @ApiModelProperty("关联该配置的层级 project/organization/side")
    private String resourceType;

    @ApiModelProperty("关联该仓库配置的资源id, 项目id 组织id 平台层为0")
    private Long resourceId;

    @ApiModelProperty("仓库是否私有")
    private Boolean repoPrivate;

    @ApiModelProperty("是否为默认仓库")
    private Boolean repoDefault;

    @ApiModelProperty("软删除 0未删除 1已删除")
    private Boolean deleted;

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

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Long getResourceId() {
        return resourceId;
    }

    public void setResourceId(Long resourceId) {
        this.resourceId = resourceId;
    }

    public Boolean getRepoPrivate() {
        return repoPrivate;
    }

    public void setRepoPrivate(Boolean repoPrivate) {
        this.repoPrivate = repoPrivate;
    }

    public Boolean getRepoDefault() {
        return repoDefault;
    }

    public void setRepoDefault(Boolean repoDefault) {
        this.repoDefault = repoDefault;
    }

    public String getResourceType() {
        return resourceType;
    }

    public void setResourceType(String resourceType) {
        this.resourceType = resourceType;
    }

    public Boolean getDeleted() {
        return deleted;
    }

    public void setDeleted(Boolean deleted) {
        this.deleted = deleted;
    }

    @Override
    public String toString() {
        return "DevopsHelmConfigDTO{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", url='" + url + '\'' +
                ", username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", resourceType='" + resourceType + '\'' +
                ", resourceId=" + resourceId +
                ", repoPrivate=" + repoPrivate +
                ", repoDefault=" + repoDefault +
                ", deleted=" + deleted +
                '}';
    }
}
