package io.choerodon.devops.infra.dto.gitlab.ci;

/**
 * @author zmf
 * @since 20-4-2
 */
public enum CachePolicy {
    PUSH("push"),
    PULL_PUSH("pull-push"),
    PULL("pull");
    private final String value;

    CachePolicy(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
