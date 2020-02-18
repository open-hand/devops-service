package io.choerodon.devops.api.vo;

import io.swagger.annotations.ApiModelProperty;

/**
 * @author zmf
 * @since 2/18/20
 */
public class DevopsPolarisSummaryVO {
    @ApiModelProperty("健康检查")
    private ClusterPolarisSummaryItemVO healthCheck;

    @ApiModelProperty("镜像检查")
    private ClusterPolarisSummaryItemVO imageCheck;

    @ApiModelProperty("网络配置")
    private ClusterPolarisSummaryItemVO networkCheck;

    @ApiModelProperty("资源分配")
    private ClusterPolarisSummaryItemVO resourceCheck;

    @ApiModelProperty("安全")
    private ClusterPolarisSummaryItemVO securityCheck;

    @ApiModelProperty("是否扫描过")
    private Boolean checked;

    public DevopsPolarisSummaryVO(){
    }

    public DevopsPolarisSummaryVO(Boolean checked) {
        this.checked = checked;
    }

    public ClusterPolarisSummaryItemVO getHealthCheck() {
        return healthCheck;
    }

    public void setHealthCheck(ClusterPolarisSummaryItemVO healthCheck) {
        this.healthCheck = healthCheck;
    }

    public ClusterPolarisSummaryItemVO getImageCheck() {
        return imageCheck;
    }

    public void setImageCheck(ClusterPolarisSummaryItemVO imageCheck) {
        this.imageCheck = imageCheck;
    }

    public ClusterPolarisSummaryItemVO getNetworkCheck() {
        return networkCheck;
    }

    public void setNetworkCheck(ClusterPolarisSummaryItemVO networkCheck) {
        this.networkCheck = networkCheck;
    }

    public ClusterPolarisSummaryItemVO getResourceCheck() {
        return resourceCheck;
    }

    public void setResourceCheck(ClusterPolarisSummaryItemVO resourceCheck) {
        this.resourceCheck = resourceCheck;
    }

    public ClusterPolarisSummaryItemVO getSecurityCheck() {
        return securityCheck;
    }

    public void setSecurityCheck(ClusterPolarisSummaryItemVO securityCheck) {
        this.securityCheck = securityCheck;
    }

    public Boolean getChecked() {
        return checked;
    }

    public void setChecked(Boolean checked) {
        this.checked = checked;
    }
}
