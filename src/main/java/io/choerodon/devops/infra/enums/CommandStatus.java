package io.choerodon.devops.infra.enums;

public enum CommandStatus {

    SUCCESS("success"),
    FAILED("failed"),
    OPERATING("operating");

    private String status;

    CommandStatus(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }
}
