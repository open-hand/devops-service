package io.choerodon.devops.infra.common.util.enums;

/**
 * Creator: Runge
 * Date: 2018/6/5
 * Time: 09:23
 * Description:
 */
public enum  IngressStatus {
    OPERATING("operating"),
    RUNNING("running"),
    FAILED("failed");

    private String status;

    IngressStatus(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
