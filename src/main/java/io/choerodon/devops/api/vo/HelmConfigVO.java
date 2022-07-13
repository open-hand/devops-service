package io.choerodon.devops.api.vo;

import java.util.Date;

import io.swagger.annotations.ApiModelProperty;

public class HelmConfigVO {
    private Long id;

    @ApiModelProperty("仓库名称")
    private String name;

    @ApiModelProperty("helm仓库地址 平台层或组织层为仓库地址前缀部分 项目层是完整的仓库地址")
    private String url;

    @ApiModelProperty("仓库账号")
    private String username;

    @ApiModelProperty("仓库密码")
    private String password;

    @ApiModelProperty("关联该配置的层级 project/organization/platform")
    private String resourceType;

    @ApiModelProperty("关联该仓库配置的资源id, 项目id 组织id 平台层为0")
    private Long resourceId;

    @ApiModelProperty("仓库是否私有")
    private Boolean repoPrivate;

    @ApiModelProperty("是否为默认仓库")
    private Boolean repoDefault;

    @ApiModelProperty("创建时间")
    private Date creationDate;

    @ApiModelProperty("创建者头像")
    private String creatorImageUrl;

    @ApiModelProperty("创建者登录名")
    private String creatorLoginName;

    @ApiModelProperty("创建者真实名称")
    private String creatorRealName;

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

    public String getResourceType() {
        return resourceType;
    }

    public void setResourceType(String resourceType) {
        this.resourceType = resourceType;
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

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public String getCreatorImageUrl() {
        return creatorImageUrl;
    }

    public void setCreatorImageUrl(String creatorImageUrl) {
        this.creatorImageUrl = creatorImageUrl;
    }

    public String getCreatorLoginName() {
        return creatorLoginName;
    }

    public void setCreatorLoginName(String creatorLoginName) {
        this.creatorLoginName = creatorLoginName;
    }

    public String getCreatorRealName() {
        return creatorRealName;
    }

    public void setCreatorRealName(String creatorRealName) {
        this.creatorRealName = creatorRealName;
    }
}
