package io.choerodon.devops.infra.enums;

/**
 * @author: 25499
 * @date: 2019/11/7 19:14
 * @description:
 */
public enum PrometheusDeployStatus {
    CREATED_PVC("created_pvc"),
    CREATED_CONFIG("created_config"),
    INSTALLED_PROMETHEUS("installed_prometheus"),
    ;

    private String status;

    PrometheusDeployStatus(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }
}
