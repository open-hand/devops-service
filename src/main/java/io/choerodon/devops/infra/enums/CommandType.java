package io.choerodon.devops.infra.enums;

public enum CommandType {

    CREATE("create"),
    STOP("stop"),
    RESTART("restart"),
    DELETE("delete"),
    UPDATE("update"),
    SYNC("sync");

    private String type;

    CommandType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }
}
