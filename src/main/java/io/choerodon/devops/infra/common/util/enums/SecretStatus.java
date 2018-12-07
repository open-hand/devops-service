package io.choerodon.devops.infra.common.util.enums;

/**
 * Created by n!Ck
 * Date: 18-12-5
 * Time: 下午5:56
 * Description:
 */
public enum SecretStatus {
    OPERATING("operation"),
    SUCCESS("running"),
    FAILED("failed"),
    DELETED("deleted");

    private String status;

    SecretStatus(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }
}
