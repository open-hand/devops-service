package io.choerodon.devops.api.vo;


import io.swagger.annotations.ApiModelProperty;

/**
 * 创建环境应用关联关系的DTO, 可以一个环境关联多个应用
 */
public class DevopsEnvApplicationCreationVO {
    @ApiModelProperty(value = "环境id")
    private Long envId;

    @ApiModelProperty(value = "该环境对应的应用id")
    private Long[] appIds;

    public Long getEnvId() {
        return envId;
    }

    public void setEnvId(Long envId) {
        this.envId = envId;
    }

    public Long[] getAppIds() {
        return appIds;
    }

    public void setAppIds(Long[] appIds) {
        this.appIds = appIds;
    }
}
