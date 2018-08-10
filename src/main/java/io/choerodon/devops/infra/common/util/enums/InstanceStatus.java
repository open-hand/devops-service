package io.choerodon.devops.infra.common.util.enums;

/**
 * Creator: Runge
 * Date: 2018/4/26
 * Time: 15:38
 * Description:
 */
public enum InstanceStatus {
    OPERATIING("operating"),
    RUNNING("running"),
    FAILED("failed"),
    STOPPED("stopped"),
    DELETED("deleted");


    private String status;

    InstanceStatus(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }
}
