package io.choerodon.devops.infra.common.util.enums;

public enum CommandType {

    CREATE("create"),
    STOP("stop"),
    RESTART("restart"),
    DELETE("delete"),
    UPDATE("update");

    private String type;

    CommandType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }
}
