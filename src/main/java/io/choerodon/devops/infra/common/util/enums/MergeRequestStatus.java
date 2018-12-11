package io.choerodon.devops.infra.common.util.enums;

/**
 * Merge request's status
 * @author zmf
 */
public enum MergeRequestStatus {
    MERGED("merged"),
    CLOSED("closed"),
    OPEN("opened");

    private final String value;

    MergeRequestStatus(String value) {
        this.value = value;
    }

    public String value() {
        return this.value;
    }
}
