package io.choerodon.devops.infra.enums;

public enum PvStatus {

    PENDING("Pending"),
    BOUND("Bound"),
    TERMINATING("Terminating"),
    OPERATING("Operating"),
    AVAILABLE("Available");

    private String status;

    PvStatus(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }
}
