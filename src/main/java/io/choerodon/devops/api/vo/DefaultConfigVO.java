package io.choerodon.devops.api.vo;

/**
 * Created by Sheep on 2019/5/5.
 */
public class DefaultConfigVO {

    private String harborConfigUrl;
    private String chartConfigUrl;

    public String getHarborConfigUrl() {
        return harborConfigUrl;
    }

    public void setHarborConfigUrl(String harborConfigUrl) {
        this.harborConfigUrl = harborConfigUrl;
    }

    public String getChartConfigUrl() {
        return chartConfigUrl;
    }

    public void setChartConfigUrl(String chartConfigUrl) {
        this.chartConfigUrl = chartConfigUrl;
    }
}
