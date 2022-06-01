package io.choerodon.devops.api.vo;

import io.swagger.annotations.ApiModelProperty;
import org.hzero.starter.keyencrypt.core.Encrypt;

/**
 * @author zongw.lee@gmail.com
 * @since 2019/03/11
 */
public class DevopsConfigVO {
    @Encrypt
    private Long id;
    @ApiModelProperty("名称")
    private String name;
    @ApiModelProperty("配置信息")
    private ConfigVO config;
    @ApiModelProperty("项目id")
    private Long projectId;
    @ApiModelProperty("组织id")
    private Long organizationId;

    @Encrypt
    @ApiModelProperty("应用服务id")
    private Long appServiceId;
    @ApiModelProperty("harbor仓库是否是私有的")
    private Boolean harborPrivate;
    @ApiModelProperty("配置类型")
    private String type;
    @ApiModelProperty("是否是自定义配置")
    private Boolean custom;
    @ApiModelProperty("版本号")
    private Long objectVersionNumber;

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

    public ConfigVO getConfig() {
        return config;
    }

    public void setConfig(ConfigVO config) {
        this.config = config;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Long getObjectVersionNumber() {
        return objectVersionNumber;
    }

    public void setObjectVersionNumber(Long objectVersionNumber) {
        this.objectVersionNumber = objectVersionNumber;
    }

    public Long getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(Long organizationId) {
        this.organizationId = organizationId;
    }

    public Long getAppServiceId() {
        return appServiceId;
    }

    public void setAppServiceId(Long appServiceId) {
        this.appServiceId = appServiceId;
    }

    public Boolean getCustom() {
        return custom;
    }

    public void setCustom(Boolean custom) {
        this.custom = custom;
    }

    public Boolean getHarborPrivate() {
        return harborPrivate;
    }

    public void setHarborPrivate(Boolean harborPrivate) {
        this.harborPrivate = harborPrivate;
    }
}
