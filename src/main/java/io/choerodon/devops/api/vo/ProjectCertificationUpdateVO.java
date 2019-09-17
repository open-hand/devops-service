package io.choerodon.devops.api.vo;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import io.swagger.annotations.ApiModelProperty;

/**
 * @author lihao
 * @date 2019-09-11 19:13
 */
public class ProjectCertificationUpdateVO {
    @ApiModelProperty("证书id")
    @NotNull(message = "error.id.null")
    private Long id;

    @ApiModelProperty("证书名称")
    @NotBlank(message = "error.name.null")
    private String name;

    @ApiModelProperty("key文件内容")
    private String keyValue;

    @ApiModelProperty("cert文件内容")
    private String certValue;

    @ApiModelProperty("域名")
    @NotBlank(message = "error.domain.null")
    private String domain;

    @ApiModelProperty("纪录版本字段")
    @NotNull(message = "error.object.version.number.null")
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

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public Long getObjectVersionNumber() {
        return objectVersionNumber;
    }

    public void setObjectVersionNumber(Long objectVersionNumber) {
        this.objectVersionNumber = objectVersionNumber;
    }
}
