package io.choerodon.devops.infra.common.util.enums;

public enum CommandStatus {

    SUCCESS("success"),
    FAILED("failed"),
    DOING("doing");

    private String status;

    CommandStatus(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }
}
