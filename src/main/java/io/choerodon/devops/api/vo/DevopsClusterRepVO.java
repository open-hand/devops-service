package io.choerodon.devops.api.vo;

import io.choerodon.devops.infra.dto.DevopsClusterDTO;
import io.swagger.annotations.ApiModelProperty;
import org.hzero.starter.keyencrypt.core.Encrypt;

public class DevopsClusterRepVO {
    @ApiModelProperty("集群id")
    @Encrypt(DevopsClusterDTO.ENCRYPT_KEY)
    private Long id;

    @ApiModelProperty("集群名称")
    private String name;

    @ApiModelProperty("是否跳过权限校验")
    private Boolean skipCheckProjectPermission;

    @ApiModelProperty("集群code")
    private String code;

    @ApiModelProperty("是否连接")
    private Boolean connect;

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


    public Boolean getConnect() {
        return connect;
    }

    public void setConnect(Boolean connect) {
        this.connect = connect;
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
}
