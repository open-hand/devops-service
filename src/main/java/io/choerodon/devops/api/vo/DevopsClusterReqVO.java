package io.choerodon.devops.api.vo;

import javax.validation.constraints.NotNull;

import io.swagger.annotations.ApiModelProperty;

public class DevopsClusterReqVO {
    @ApiModelProperty("集群名称 / 必需")
    @NotNull(message = "error.name.null")
    private String name;

    @ApiModelProperty("集群编码 / 必需")
    @NotNull(message = "error.code.null")
    private String code;

    @ApiModelProperty("集群描述 / 非必需")
    private String description;

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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
