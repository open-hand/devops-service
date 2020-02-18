package io.choerodon.devops.infra.enums;

/**
 * @author zmf
 * @since 2/18/20
 */
public enum PolarisSeverity {
    ERROR("error"),
    WARNING("warning"),
    IGNORE("ignore");
    private String value;

    PolarisSeverity(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
