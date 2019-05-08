package io.choerodon.devops.api.dto.sonar;

import java.util.List;

/**
 * Created by Sheep on 2019/5/6.
 */
public class Measure {

    private String metric;
    private String value;
    private List<Period> periods;


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

    public List<Period> getPeriods() {
        return periods;
    }

    public void setPeriods(List<Period> periods) {
        this.periods = periods;
    }
}
