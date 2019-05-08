package io.choerodon.devops.api.dto.sonar;

import java.util.List;

/**
 * Created by Sheep on 2019/5/6.
 */
public class Facet {
    private String property;
    private List<Value> values;

    public String getProperty() {
        return property;
    }

    public void setProperty(String property) {
        this.property = property;
    }

    public List<Value> getValues() {
        return values;
    }

    public void setValues(List<Value> values) {
        this.values = values;
    }
}
