package io.choerodon.devops.infra.enums;

public enum DevopsCiSonarQualityGateConditionMetricTypeEnum {
    NEW_VIOLATIONS("new_violations"),
    NEW_BLOCKER_VIOLATIONS("new_blocker_violations"),
    NEW_CRITICAL_VIOLATIONS("new_critical_violations"),
    NEW_MAJOR_VIOLATIONS("new_major_violations"),
    NEW_MINOR_VIOLATIONS("new_minor_violations"),
    NEW_DUPLICATED_LINES_DENSITY("new_duplicated_lines_density"),

    VIOLATIONS("violations"),
    BLOCKER_VIOLATIONS("blocker_violations"),
    CRITICAL_VIOLATIONS("critical_violations"),
    MAJOR_VIOLATIONS("major_violations"),
    MINOR_VIOLATIONS("minor_violations"),
    DUPLICATED_LINES_DENSITY("duplicated_lines_density");

    private final String metric;

    public String getMetric() {
        return this.metric;
    }

    DevopsCiSonarQualityGateConditionMetricTypeEnum(String metric) {
        this.metric = metric;
    }
}
