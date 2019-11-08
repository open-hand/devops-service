package io.choerodon.devops.infra.enums;

/**
 * @author: 25499
 * @date: 2019/11/7 19:14
 * @description:
 */
public enum PrometheusDeployStatus {
    CREATE_PVC_SUCCESS("create_pvc_success"),
    CREATE_CONFIG_SUCCESS("create_config_success"),
    INSTALL_PROMETHEUS_SUCCESS("install_prometheus_success"),
    INSTALL_PROMETHEUS_FAIL("install_prometheus_fail"),
    ;

    private String stage;

    PrometheusDeployStatus(String stage) {
        this.stage = stage;
    }

    public String getStage() {
        return stage;
    }

    public void setStage(String stage) {
        this.stage = stage;
    }
}
