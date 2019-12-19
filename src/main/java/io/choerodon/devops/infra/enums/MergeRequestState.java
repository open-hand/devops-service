package io.choerodon.devops.infra.enums;

/**
 * Merge Request的状态
 *
 * @author zmf
 * @since 12/5/19
 */
public enum MergeRequestState {
    OPENED("opened"),
    CLOSED("closed"),
    MERGED("merged");

    private final String value;

    MergeRequestState(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
