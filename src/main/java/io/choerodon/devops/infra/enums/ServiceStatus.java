package io.choerodon.devops.infra.enums;

/**
 * Created by Zenger on 2018/5/17.
 */
public enum ServiceStatus {

    OPERATIING("operating"),
    RUNNING("running"),
    FAILED("failed"),
    DELETED("deleted");

    private String status;

    ServiceStatus(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }
}
