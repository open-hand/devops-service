package io.choerodon.devops.api.vo;


import io.swagger.annotations.ApiModelProperty;

/**
 * 创建环境应用关联关系的DTO, 可以一个环境关联多个应用
 */
public class DevopsEnvAppServiceVO {
    @ApiModelProperty(value = "环境id")
    private Long envId;

    @ApiModelProperty(value = "该环境对应的应用id")
    private Long[] appServiceIds;

    public Long getEnvId() {
        return envId;
    }

    public void setEnvId(Long envId) {
        this.envId = envId;
    }

    public Long[] getAppServiceIds() {
        return appServiceIds;
    }

    public void getAppServiceIds(Long[] appServiceIds) {
        this.appServiceIds = appServiceIds;
    }
}
