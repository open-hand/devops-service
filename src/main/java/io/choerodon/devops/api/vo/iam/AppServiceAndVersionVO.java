package io.choerodon.devops.api.vo.iam;

import io.swagger.annotations.ApiModelProperty;

/**
 * 此DTO用于Feign请求服务版本信息
 */
public class AppServiceAndVersionVO {
    @ApiModelProperty("应用服务主键")
    private Long id;
    @ApiModelProperty("应用服务名称")
    private String name;
    @ApiModelProperty("应用服务编码")
    private String code;
    @ApiModelProperty("应用服务类型")
    private String type;
    @ApiModelProperty("应用服务版本主键")
    private Long versionId;
    @ApiModelProperty("应用服务版本名称")
    private String version;
    @ApiModelProperty("应用服务版本状态")
    private String versionStatus;

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

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Long getVersionId() {
        return versionId;
    }

    public void setVersionId(Long versionId) {
        this.versionId = versionId;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getVersionStatus() {
        return versionStatus;
    }

    public void setVersionStatus(String versionStatus) {
        this.versionStatus = versionStatus;
    }

}
