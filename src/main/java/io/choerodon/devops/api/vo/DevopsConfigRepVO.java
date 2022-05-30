package io.choerodon.devops.api.vo;

import io.swagger.annotations.ApiModelProperty;

/**
 * @author zhaotianxin
 * @since 2019/9/3
 */
public class DevopsConfigRepVO {
    @ApiModelProperty("harbor配置信息")
    private DevopsConfigVO harbor;
    @ApiModelProperty("chart配置信息")
    private DevopsConfigVO chart;
    @ApiModelProperty("harbor仓库是否是私有库")
    private Boolean harborPrivate;

    public Boolean getHarborPrivate() {
        return harborPrivate;
    }

    public void setHarborPrivate(Boolean harborPrivate) {
        this.harborPrivate = harborPrivate;
    }

    public DevopsConfigVO getHarbor() {
        return harbor;
    }

    public void setHarbor(DevopsConfigVO harbor) {
        this.harbor = harbor;
    }

    public DevopsConfigVO getChart() {
        return chart;
    }

    public void setChart(DevopsConfigVO chart) {
        this.chart = chart;
    }
}
