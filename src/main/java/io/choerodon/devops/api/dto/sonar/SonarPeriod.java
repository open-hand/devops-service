package io.choerodon.devops.api.dto.sonar;

/**
 * Created by Sheep on 2019/5/7.
 */
public class SonarPeriod {

    private String date;
    private String mode;
    private String parameter;


    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public String getParameter() {
        return parameter;
    }

    public void setParameter(String parameter) {
        this.parameter = parameter;
    }
}
