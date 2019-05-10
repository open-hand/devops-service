package io.choerodon.devops.api.dto.sonar;

import java.util.List;

/**
 * Created by Sheep on 2019/5/7.
 */
public class SonarTableMeasure {


    private String metric;
    private List<SonarHistroy> history;

    public String getMetric() {
        return metric;
    }

    public void setMetric(String metric) {
        this.metric = metric;
    }

    public List<SonarHistroy> getHistory() {
        return history;
    }

    public void setHistory(List<SonarHistroy> history) {
        this.history = history;
    }
}
