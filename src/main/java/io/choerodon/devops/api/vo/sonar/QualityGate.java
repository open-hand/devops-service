package io.choerodon.devops.api.vo.sonar;

import java.util.List;

public class QualityGate {
    private String id;
    private String name;

    private List<QualityGateCondition> conditions;

    public List<QualityGateCondition> getConditions() {
        return conditions;
    }

    public void setConditions(List<QualityGateCondition> conditions) {
        this.conditions = conditions;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
