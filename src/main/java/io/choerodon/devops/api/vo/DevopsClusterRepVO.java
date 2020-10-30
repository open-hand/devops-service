package io.choerodon.devops.api.vo;

import io.swagger.annotations.ApiModelProperty;
import org.hzero.starter.keyencrypt.core.Encrypt;

public class DevopsClusterRepVO {
    @ApiModelProperty("集群id")
    @Encrypt
    private Long id;

    @ApiModelProperty("集群名称")
    private String name;

    @ApiModelProperty("是否跳过权限校验")
    private Boolean skipCheckProjectPermission;

    @ApiModelProperty("集群code")
    private String code;

    @ApiModelProperty
    private String status;

    @ApiModelProperty("集群描述")
    private String description;

    @ApiModelProperty("集群token")
    private String token;

    private String choerodonId;

    @ApiModelProperty("创建者id")
    private Long createdBy;

    @ApiModelProperty("纪录版本字段")
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

    public Long getObjectVersionNumber() {
        return objectVersionNumber;
    }

    public void setObjectVersionNumber(Long objectVersionNumber) {
        this.objectVersionNumber = objectVersionNumber;
    }

    public Long getCreatedBy() {
        return createdBy;
    }

    public DevopsClusterRepVO setCreatedBy(Long createdBy) {
        this.createdBy = createdBy;
        return this;
    }

    public String getStatus() {
        return status;
    }

    public DevopsClusterRepVO setStatus(String status) {
        this.status = status;
        return this;
    }
}
