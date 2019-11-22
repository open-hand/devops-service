package io.choerodon.devops.infra.enums;

/**
 * @author wanghao
 * @Date 2019/11/15 16:42
 */
public enum PrometheusPVTypeEnum {

    PORMETHEUS_PV("Pormetheus-PV"),
    GRAFANA_PV("Grafana-PV"),
    ALERTMANAGER_PV("Alertmanager-PV");

    private final String value;

    PrometheusPVTypeEnum(String value) {
        this.value = value;
    }

    public String value() {
        return this.value;
    }
}
