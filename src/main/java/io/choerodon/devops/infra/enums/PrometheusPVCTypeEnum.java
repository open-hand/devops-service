package io.choerodon.devops.infra.enums;

/**
 * @author wanghao
 * @Date 2019/11/15 16:42
 */
public enum PrometheusPVCTypeEnum {

    PROMETHEUS_PVC("prometheus-pvc"),
    GRAFANA_PVC("grafana-pvc"),
    ALERTMANAGER_PVC("alertmanager-pvc");

    private final String value;

    PrometheusPVCTypeEnum(String value) {
        this.value = value;
    }

    public String value() {
        return this.value;
    }
}
