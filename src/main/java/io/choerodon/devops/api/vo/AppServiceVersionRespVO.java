package io.choerodon.devops.api.vo;

import java.io.Serializable;
import java.util.Date;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.hzero.starter.keyencrypt.core.Encrypt;


@ApiModel(value = "应用服务版本VO")
public class AppServiceVersionRespVO implements Serializable {
    @Encrypt
    private Long id;
    @ApiModelProperty("应用服务版本名")
    private String version;
    @ApiModelProperty("版本关联的commit_sha")
    private String commit;
    @ApiModelProperty("版本关联的应用服务名称")
    private String appServiceName;
    @ApiModelProperty("版本关联的应用服务编码")
    private String appServiceCode;
    @Encrypt
    @ApiModelProperty("版本关联的应用服务id")
    private Long appServiceId;
    @ApiModelProperty("版本关联的应用服务状态")
    private Boolean appServiceStatus;
    @ApiModelProperty("版本的创建日期")
    private Date creationDate;
    @ApiModelProperty("是否有版本的权限")
    private Boolean permission;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getCommit() {
        return commit;
    }

    public void setCommit(String commit) {
        this.commit = commit;
    }

    public String getAppServiceName() {
        return appServiceName;
    }

    public void setAppServiceName(String appServiceName) {
        this.appServiceName = appServiceName;
    }

    public String getAppServiceCode() {
        return appServiceCode;
    }

    public void setAppServiceCode(String appServiceCode) {
        this.appServiceCode = appServiceCode;
    }

    public Long getAppServiceId() {
        return appServiceId;
    }

    public void setAppServiceId(Long appServiceId) {
        this.appServiceId = appServiceId;
    }

    public Boolean getAppServiceStatus() {
        return appServiceStatus;
    }

    public void setAppServiceStatus(Boolean appServiceStatus) {
        this.appServiceStatus = appServiceStatus;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public Boolean getPermission() {
        return permission;
    }

    public void setPermission(Boolean permission) {
        this.permission = permission;
    }
}
