package io.choerodon.devops.api.dto.sonar;

import java.util.List;

/**
 * Created by Sheep on 2019/5/6.
 */
public class SonarComponent {

    private Component component;
    private List<SonarPeriod> periods;


    public Component getComponent() {
        return component;
    }

    public void setComponent(Component component) {
        this.component = component;
    }

    public List<SonarPeriod> getPeriods() {
        return periods;
    }

    public void setPeriods(List<SonarPeriod> periods) {
        this.periods = periods;
    }
}
