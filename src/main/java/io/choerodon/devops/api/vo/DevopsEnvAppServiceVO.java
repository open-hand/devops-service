package io.choerodon.devops.api.vo;


import io.swagger.annotations.ApiModelProperty;

/**
 * 环境和应用服务关联关系, 可以一个环境关联多个应用
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
