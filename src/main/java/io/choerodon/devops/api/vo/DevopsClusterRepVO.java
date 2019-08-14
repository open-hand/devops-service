package io.choerodon.devops.api.vo;

import io.swagger.annotations.ApiModelProperty;

public class DevopsClusterRepVO {

    private Long id;
    private String name;
    private Boolean skipCheckProjectPermission;
    private String code;
    private Boolean connect;
    @ApiModelProperty(value = "是否需要升级")
    private Boolean upgrade;
    private String upgradeMessage;
    private String description;
    private String token;
    private String choerodonId;
    private Long createBy;

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

    public Boolean getSkipCheckProjectPermission() {
        return skipCheckProjectPermission;
    }

    public void setSkipCheckProjectPermission(Boolean skipCheckProjectPermission) {
        this.skipCheckProjectPermission = skipCheckProjectPermission;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }


    public Boolean getConnect() {
        return connect;
    }

    public void setConnect(Boolean connect) {
        this.connect = connect;
    }

    public Boolean getUpgrade() {
        return upgrade;
    }

    public void setUpgrade(Boolean upgrade) {
        this.upgrade = upgrade;
    }

    public String getUpgradeMessage() {
        return upgradeMessage;
    }

    public void setUpgradeMessage(String upgradeMessage) {
        this.upgradeMessage = upgradeMessage;
    }


    public Long getCreateBy() {
        return createBy;
    }

    public void setCreateBy(Long createBy) {
        this.createBy = createBy;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getChoerodonId() {
        return choerodonId;
    }

    public void setChoerodonId(String choerodonId) {
        this.choerodonId = choerodonId;
    }
}
