package io.choerodon.devops.api.vo.sonar;

import java.util.List;

/**
 * Created by Sheep on 2019/5/6.
 */
public class Component {

    private String name;
    private String key;
    private List<Measure> measures;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public List<Measure> getMeasures() {
        return measures;
    }

    public void setMeasures(List<Measure> measures) {
        this.measures = measures;
    }
}
