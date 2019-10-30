package io.choerodon.devops.infra.enums;

/**
 * @author zhaotianxin
 * @since 2019/10/29
 */
public enum ClusterResourceStatus {
    INSTALLING("installing"),
    SUCCESS("success"),
    FAILED("failed"),
    UNLOADING("unloading");
    private String status;

    ClusterResourceStatus(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }
}
