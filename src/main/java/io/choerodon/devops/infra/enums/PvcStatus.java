package io.choerodon.devops.infra.enums;

public enum PvcStatus {
    PENDING("pending"),
    BOUND("bound"),
    LOST("lost"),
    TERMINATING("terminating");

    private String status;

    PvcStatus(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }
}
