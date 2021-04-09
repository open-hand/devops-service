package io.choerodon.devops.api.vo.sonar;

public class SonarTable {

    /**
     * 扫描类型（bug，异味等）
     */
    private String metric;
    /**
     * 类型对应值
     */
    private String value;
    /**
     * 组织-项目-应用服务
     */
    private String component;

    private Boolean bestValue;

    public String getMetric() {
        return metric;
    }

    public void setMetric(String metric) {
        this.metric = metric;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getComponent() {
        return component;
    }

    public void setComponent(String component) {
        this.component = component;
    }

    public Boolean getBestValue() {
        return bestValue;
    }

    public void setBestValue(Boolean bestValue) {
        this.bestValue = bestValue;
    }
}
