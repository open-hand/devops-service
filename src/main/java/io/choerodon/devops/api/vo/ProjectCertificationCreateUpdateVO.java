package io.choerodon.devops.api.vo;

import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotNull;

public class ProjectCertificationCreateUpdateVO {
    /**
     * 这里要和 {@link org.springframework.web.bind.annotation.ModelAttribute } 一起使用,
     * 主键加密组件暂时有bug. 要手动处理, 时间 2020年07月30日15:31:55
     */
    @ApiModelProperty("证书id")
    private String id;

    @ApiModelProperty("证书名称")
    @NotNull(message = "{devops.name.null}")
    private String name;

    @ApiModelProperty("key文件内容")
    private String keyValue;

    @ApiModelProperty("cert文件内容")
    private String certValue;

    @ApiModelProperty("域名")
    private String domain;

    @ApiModelProperty("是否跳过权限校验")
    private Boolean skipCheckProjectPermission;

    @ApiModelProperty("纪录版本字段")
    private Long objectVersionNumber;

    @ApiModelProperty("操作类型")
    private String type;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
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

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Long getObjectVersionNumber() {
        return objectVersionNumber;
    }

    public void setObjectVersionNumber(Long objectVersionNumber) {
        this.objectVersionNumber = objectVersionNumber;
    }
}
