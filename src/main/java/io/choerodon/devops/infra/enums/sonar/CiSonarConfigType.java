package io.choerodon.devops.infra.enums.sonar;

/**
 * @author zmf
 * @since 2020/6/16
 */
public enum CiSonarConfigType {
    DEFAULT("default"),
    CUSTOM("custom");

    private final String value;

    CiSonarConfigType(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }
}
