package io.choerodon.devops.infra.common.util.enums;

public enum CommandStatus {

    SUCCESS("success"),
    FAILED("failed"),
    DOING("doing");

    private String commandStatus;

    CommandStatus(String commandStatus) {
        this.commandStatus = commandStatus;
    }

    public String getCommandStatus() {
        return commandStatus;
    }

    public void setCommandStatus(String commandStatus) {
        this.commandStatus = commandStatus;
    }
}
