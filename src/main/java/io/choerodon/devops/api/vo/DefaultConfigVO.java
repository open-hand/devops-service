package io.choerodon.devops.api.vo;

import io.swagger.annotations.ApiModelProperty;

/**
 * Created by Sheep on 2019/5/5.
 */
public class DefaultConfigVO {
    @ApiModelProperty("harbor地址")
    private String harborConfigUrl;
    @ApiModelProperty("chart地址")
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
