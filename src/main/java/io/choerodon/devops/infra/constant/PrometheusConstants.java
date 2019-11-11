package io.choerodon.devops.infra.constant;

/**
 * @author: 25499
 * @date: 2019/11/7 19:14
 * @description:
 */
public class PrometheusConstants {
    public static final String SUCCESSED = "successed";
    public static final String OPERATING = "operating";
    public static final String WAITING = "waiting";
    public static final String FAILED = "failed";

    public interface PvcName {
        String prometheus_pvcName = "prometheus-pvc";
        String grafana_pvcName = "grafana-pvc";
        String alertManager_pvcName = "alertmanager-pvc";
    }
}
