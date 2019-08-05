package io.choerodon.devops.api.vo.sonar;

import java.util.List;

/**
 * Created by Sheep on 2019/5/27.
 */
public class Projects {

    private List<Component> components;


    public List<Component> getComponents() {
        return components;
    }

    public void setComponents(List<Component> components) {
        this.components = components;
    }
}
