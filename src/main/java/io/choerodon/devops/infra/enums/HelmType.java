package io.choerodon.devops.infra.enums;

import java.util.HashMap;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Created by Zenger on 2017/11/14.
 */
public enum HelmType {
    HELM_ERROR("helm_release_error"),
    HELM_INSTALL_JOB_INFO("helm_install_job_info"),
    HELM_INSTALL_RESOURCE_INFO("helm_install_resource_info"),
    HELM_JOB_LOG("helm_job_log"),
    HELM_UPGRADE_RESOURCE_INFO("helm_upgrade_resource_info"),
    HELM_RELEASE_ROLLBACK("helm_release_rollback"),
    HELM_RELEASE_START("helm_release_start"),
    HELM_RELEASE_STOP("helm_release_stop"),
    HELM_RELEASE_DELETE("helm_release_delete"),
    HELM_UPGRADE_JOB_INFO("helm_upgrade_job_info"),
    NETWORK_SERVICE("network_service"),
    NETWORK_INGRESS("network_ingress"),
    NETWORK_SERVICE_DELETE("network_service_delete"),
    NETWORK_INGRESS_DELETE("network_ingress_delete"),
    RESOURCE_UPDATE("resource_update"),
    HELM_RELEASES("helm_releases"),
    RESOURCE_DELETE("resource_delete"),
    HELM_RELEASE_INSTALL_FAILED("helm_release_install_failed"),
    HELM_RELEASE_UPGRADE_FAILED("helm_release_upgrade_failed"),
    HELM_RELEASE_ROLLBACK_FAILED("helm_release_rollback_failed"),
    HELM_RELEASE_START_FAILED("helm_release_start_failed"),
    HELM_RELEASE_STOP_FAILED("helm_release_stop_failed"),
    HELM_RELEASE_DELETE_FAILED("helm_release_delete_failed"),
    KUBERNETES_GET_LOGS("kubernetes_get_logs"),
    HELM_RELEASE_GET_CONTENT("helm_release_get_content"),
    HELM_RELEASE_GET_CONTENT_FAILED("helm_release_get_content_failed"),
    COMMAND_NOT_SEND("command_not_send"),
    NETWORK_SERVICE_UPDATE("network_service_update"),
    NETWORK_SERVICE_FAILED("network_service_failed"),
    NETWORK_SERVICE_DELETE_FAILED("network_service_delete_failed"),
    NETWORK_INGRESS_FAILED("network_ingress_failed"),
    NETWORK_INGRESS_DELETE_FAILED("network_ingress_delete_failed"),
    RESOURCE_SYNC("resource_sync"),
    HELM_JOB_EVENT("helm_job_event"),
    HELM_POD_EVENT("helm_pod_event"),
    WORKLOAD_POD_EVENT("workload_pod_event"),
    GIT_OPS_SYNC_EVENT("git_ops_sync_event"),
    RESOURCE_STATUS_SYNC_EVENT("resource_status_sync_event"),
    RESOURCE_STATUS_SYNC("resource_status_sync"),
    CERT_ISSUED("cert_issued"),
    NAMESPACE_INFO("namespace_info"),
    UPGRADE_CLUSTER("upgrade"),
    CERT_FAILED("cert_failed"),
    EXECUTE_TEST_SUCCEED("execute_test_succeed"),
    EXECUTE_TEST_FAILED("execute_test_failed"),
    TEST_POD_EVENT("test_pod_event"),
    TEST_POD_UPDATE("test_pod_update"),
    TEST_JOB_LOG("test_job_log"),
    GET_TEST_APP_STATUS("get_test_app_status"),
    TEST_STATUS_RESPONSE("test_status_response"),
    TEST_STATUS("test_status"),
    TEST_EXECUTE("test_execute"),
    CONFIG_UPDATE("config_update"),
    CERT_MANAGER_INSTALL("cert_manager_install"),
    CERT_MANAGER_STATUS("cert_manager_status"),
    CERT_MANAGER_UNINSTALL("cert_manager_uninstall"),
    CRD_UNLOAD("crd_unload"),
    CRD_UNLOAD_STATUS("crd_unload_status"),
    CHART_MUSEUM_AUTHENTICATION("chart_museum_authentication"),
    OPERATE_DOCKER_REGISTRY_SECRET_FAILED("operate_docker_registry_secret_failed"),
    OPERATE_DOCKER_REGISTRY_SECRET("operate_docker_registry_secret"),
    /**
     * 删除pod，由DevOps服务发到agent，agent返回也是这个
     */
    DELETE_POD("delete_pod"),
    /**
     * agent启动时将集群的基本信息发送过来，如版本，namespace数量等
     */
    CLUSTER_INFO("cluster_info"),
    /**
     * polaris扫描集群或集群的某个namespace
     */
    POLARIS_SCAN_CLUSTER("polaris_scan_cluster"),
    POD_METRICS_SYNC("pod_metrics_sync"),
    OPERATE_POD_COUNT_FAILED("operate_pod_count_failed"),
    OPERATE_POD_COUNT_SUCCEED("operate_pod_count_succeed"),
    NODE_SYNC("node_sync");

    private static HashMap<String, HelmType> valuesMap = new HashMap<>(6);

    static {
        HelmType[] var0 = values();

        for (HelmType accessLevel : var0) {
            valuesMap.put(accessLevel.value, accessLevel);
        }

    }

    public final String value;

    HelmType(String value) {
        this.value = value;
    }

    /**
     * 根据string类型返回枚举类型
     *
     * @param value String
     */
    @JsonCreator
    public static HelmType forValue(String value) {
        return valuesMap.get(value);
    }

    @JsonValue
    public String toValue() {
        return this.value;
    }

    @Override
    public String toString() {
        return this.value;
    }
}
