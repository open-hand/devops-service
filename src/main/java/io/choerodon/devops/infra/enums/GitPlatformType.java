package io.choerodon.devops.infra.enums;

/**
 * @author zmf
 */
public enum GitPlatformType {
    GITHUB("github"), GITLAB("gitlab"), UNKNOWN("unknown");
    private String value;

    GitPlatformType(String value) {
        this.value = value;
    }

    public String value() {
        return this.value;
    }

    public static GitPlatformType from(String value) {
        if (GITHUB.value.equals(value)) {
            return GITHUB;
        } else if (GITLAB.value.equals(value)) {
            return GITLAB;
        } else {
            return UNKNOWN;
        }
    }
}
