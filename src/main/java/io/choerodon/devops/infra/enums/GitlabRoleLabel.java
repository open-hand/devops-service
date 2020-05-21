package io.choerodon.devops.infra.enums;

/**
 * 参考 io.choerodon.iam.infra.enums.RoleLabelEnum (在iam项目中)
 *
 * @author zmf
 * @since 20-5-21
 */
public enum GitlabRoleLabel {
    OWNER("GITLAB_OWNER"),
    DEVELOPER("GITLAB_DEVELOPER");

    private String value;

    GitlabRoleLabel(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static GitlabRoleLabel forValue(String value) {
        for (int i = 0; i < values().length; i++) {
            if (values()[i].getValue().equals(value)) {
                return values()[i];
            }
        }
        return null;
    }
}
