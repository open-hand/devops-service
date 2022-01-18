package io.choerodon.devops.api.vo.sonar;

/**
 * Created by Sheep on 2019/5/6.
 */
public class Period {

    private String value;
    private Boolean bestValue;

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Boolean getBestValue() {
        return bestValue;
    }

    public void setBestValue(Boolean bestValue) {
        this.bestValue = bestValue;
    }
}
