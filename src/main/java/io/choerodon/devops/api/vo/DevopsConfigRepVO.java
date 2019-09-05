package io.choerodon.devops.api.vo;

/**
 * @author zhaotianxin
 * @since 2019/9/3
 */
public class DevopsConfigRepVO {
    private DevopsConfigVO harbor;
    private DevopsConfigVO chart;
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
