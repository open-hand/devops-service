package io.choerodon.devops.infra.constant;

/**
 * @author: 25499
 * @date: 2019/11/7 19:14
 * @description:
 */
public class PrometheusConstants {
    public static final String CREATE_PVC_SUCCESS = "create_pvc_success";
    public static final String CREATE_CONFIG_SUCCESS = "create_config_success";
    public static final String INSTALL_PROMETHEUS_SUCCESS = "install_prometheus_success";
    public static final String INSTALL_PROMETHEUS_FAIL = "install_prometheus_fail";


    public interface PvcName {
        String prometheus_pvcName = "prometheus-pvc";
        String grafana_pvcName = "grafana-pvc";
        String alertManager_pvcName = "alertmanager-pvc";
    }
}
