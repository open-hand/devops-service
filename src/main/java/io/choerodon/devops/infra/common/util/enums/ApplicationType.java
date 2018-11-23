package io.choerodon.devops.infra.common.util.enums;

public enum ApplicationType {

    TEST("test"),
    NORMAL("normal");

    private String type;

    ApplicationType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }
}
