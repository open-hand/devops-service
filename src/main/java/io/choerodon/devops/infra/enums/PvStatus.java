package io.choerodon.devops.infra.enums;

public enum PvStatus {

    PENDING("pending"),
    BOUND("bound"),
    TERMINATING("terminating"),
    OPERATING("operating");

    private String status;

    PvStatus(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }
}
