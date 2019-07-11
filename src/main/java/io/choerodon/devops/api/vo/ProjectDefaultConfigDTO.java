package io.choerodon.devops.api.vo;

/**
 * Created by Sheep on 2019/5/5.
 */
public class ProjectDefaultConfigDTO {

    private String harborConfigName;
    private String chartConfigName;
    private Boolean harborIsPrivate;


    public String getHarborConfigName() {
        return harborConfigName;
    }

    public void setHarborConfigName(String harborConfigName) {
        this.harborConfigName = harborConfigName;
    }

    public String getChartConfigName() {
        return chartConfigName;
    }

    public void setChartConfigName(String chartConfigName) {
        this.chartConfigName = chartConfigName;
    }

    public boolean isHarborIsPrivate() {
        return harborIsPrivate;
    }

    public void setHarborIsPrivate(Boolean harborIsPrivate) {
        this.harborIsPrivate = harborIsPrivate;
    }
}
