package io.choerodon.devops.infra.enums;

/**
 * @author wanghao
 * @Date 2019/11/15 16:42
 */
public enum PrometheusPVCTypeEnum {

    PROMETHEUS_PVC("Prometheus-PVC"),
    GRAFANA_PVC("Grafana-PVC"),
    ALERTMANAGER_PVC("Alertmanager-PVC");

    private final String value;

    PrometheusPVCTypeEnum(String value) {
        this.value = value;
    }

    public String value() {
        return this.value;
    }
}
