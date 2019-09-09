package io.choerodon.devops.infra.enums;

/**
 * @author zongw.lee@gmail.com
 * @since 2019/03/12
 */
public enum ProjectConfigType {
    HARBOR("harbor"),
    SONAR("sonar"),
    CHART("chart");

    private String type;

    ProjectConfigType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }
}
