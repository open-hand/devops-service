package io.choerodon.devops.api.dto.sonar;

/**
 * Created by Sheep on 2019/5/7.
 */

public class SonarHistroy {

    private String date;
    private String value;

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
