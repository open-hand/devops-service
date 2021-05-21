package io.choerodon.devops.infra.enums;

public enum DevopsIssueRelObjectTypeEnum {
    BRANCH("branch"),
    COMMIT("commit");

    private String value;

    DevopsIssueRelObjectTypeEnum(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
