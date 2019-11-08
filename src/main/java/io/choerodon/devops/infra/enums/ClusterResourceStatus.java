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

    interface Prometheus{
        public static  final String CREATE_PVC_SUCCESS="create_pvc_success";
        public static  final String CREATE_CONFIG_SUCCESS="create_config_success";
        public static  final String INSTALL_PROMETHEUS_SUCCESS="install_prometheus_success";
        public static  final String INSTALL_PROMETHEUS_FAIL="install_prometheus_fail";
    }

    ClusterResourceStatus(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }
}
