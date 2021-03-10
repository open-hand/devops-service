package io.choerodon.devops.api.vo;

import io.swagger.annotations.ApiModelProperty;

public class MiddlewareRedisDeployVO extends MarketInstanceCreationRequestVO {
    @ApiModelProperty("pvc名称")
    private String pvcName;

    @ApiModelProperty("部署模式")
    private String mode;

    @ApiModelProperty("中间件版本")
    private String version;

    public String getPvcName() {
        return pvcName;
    }

    public void setPvcName(String pvcName) {
        this.pvcName = pvcName;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }
}
