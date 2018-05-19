package io.choerodon.devops.infra.common.util.enums;

public enum CommandType {

    CREATE("create"),
    STOP("stop"),
    RESTART("restart"),
    DELETE("delete"),
    UPDATE("update");

    private String commandType;

    CommandType(String commandType) {
        this.commandType = commandType;
    }

    public String getCommandType() {
        return commandType;
    }

    public void setCommandType(String commandType) {
        this.commandType = commandType;
    }
}
