package io.choerodon.devops.api.dto.sonar;

/**
 * Created by Sheep on 2019/5/6.
 */
public class Period {

    private Long index;
    private String value;


    public Long getIndex() {
        return index;
    }

    public void setIndex(Long index) {
        this.index = index;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
