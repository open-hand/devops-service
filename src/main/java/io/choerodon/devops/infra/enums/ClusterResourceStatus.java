package io.choerodon.devops.infra.enums;

/**
 * @author zhaotianxin
 * @since 2019/10/29
 */
public enum ClusterResourceStatus {
    UNINSTALL("uninstall"),
    PROCESSING("processing"),
    AVAILABLE("available"),
    DISABLED("disabled");
    private String status;

    ClusterResourceStatus(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }
}
