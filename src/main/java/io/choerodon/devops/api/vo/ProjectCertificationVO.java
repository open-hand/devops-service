package io.choerodon.devops.api.vo;

import javax.validation.constraints.NotNull;

import io.swagger.annotations.ApiModelProperty;

public class ProjectCertificationVO {
    @ApiModelProperty("证书id")
    private Long id;

    @ApiModelProperty("证书名称")
    @NotNull(message = "error.name.null")
    private String name;

    @ApiModelProperty("key文件内容")
    private String keyValue;

    @ApiModelProperty("cert文件内容")
    private String certValue;

    @ApiModelProperty("域名")
    @NotNull(message = "error.domain.null")
    private String domain;

    @ApiModelProperty("是否跳过权限校验")
    private Boolean skipCheckProjectPermission;

    @ApiModelProperty("纪录版本字段")
    private Long objectVersionNumber;

    public ProjectCertificationVO(String name, String domain) {
        this.name = name;
        this.domain = domain;
    }

    public ProjectCertificationVO(Long id, String name, String domain, Boolean skipCheckProjectPermission, Long objectVersionNumber) {
        this.id = id;
        this.name = name;
        this.domain = domain;
        this.skipCheckProjectPermission = skipCheckProjectPermission;
        this.objectVersionNumber = objectVersionNumber;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getKeyValue() {
        return keyValue;
    }

    public void setKeyValue(String keyValue) {
        this.keyValue = keyValue;
    }

    public String getCertValue() {
        return certValue;
    }

    public void setCertValue(String certValue) {
        this.certValue = certValue;
    }

    public Boolean getSkipCheckProjectPermission() {
        return skipCheckProjectPermission;
    }

    public void setSkipCheckProjectPermission(Boolean skipCheckProjectPermission) {
        this.skipCheckProjectPermission = skipCheckProjectPermission;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getObjectVersionNumber() {
        return objectVersionNumber;
    }

    public void setObjectVersionNumber(Long objectVersionNumber) {
        this.objectVersionNumber = objectVersionNumber;
    }
}
