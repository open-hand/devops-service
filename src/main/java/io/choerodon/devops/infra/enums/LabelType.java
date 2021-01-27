package io.choerodon.devops.infra.enums;

/**
 * 参考 io.choerodon.iam.infra.enums.RoleLabelEnum (在iam项目中)
 */
public enum LabelType {
    PROJECT_ADMIN("PROJECT_ADMIN"),
    TENANT_ADMIN("TENANT_ADMIN"),
    GITLAB_PROJECT_OWNER("GITLAB_OWNER"),
    GITLAB_PROJECT_DEVELOPER("GITLAB_DEVELOPER");

    private final String value;

    LabelType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static LabelType forValue(String value) {
        for (int i = 0; i < values().length; i++) {
            if (values()[i].getValue().equals(value)) {
                return values()[i];
            }
        }
        return null;
    }
}
