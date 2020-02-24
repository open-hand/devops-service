package io.choerodon.devops.api.vo;

import io.swagger.annotations.ApiModelProperty;

/**
 * @author zmf
 * @since 2/24/20
 */
public class AppServiceSimpleVO {
    @ApiModelProperty("应用服务id")
    private Long appServiceId;
    @ApiModelProperty("应用服务名称")
    private String appServiceName;
    @ApiModelProperty("应用服务code")
    private String appServiceCode;

    public AppServiceSimpleVO() {
    }

    public AppServiceSimpleVO(Long appServiceId, String appServiceName, String appServiceCode) {
        this.appServiceId = appServiceId;
        this.appServiceName = appServiceName;
        this.appServiceCode = appServiceCode;
    }

    public Long getAppServiceId() {
        return appServiceId;
    }

    public void setAppServiceId(Long appServiceId) {
        this.appServiceId = appServiceId;
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
}
