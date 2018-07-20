package io.choerodon.devops.infra.common.util.enums;

import java.util.HashMap;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Created by Zenger on 2017/11/14.
 */
public enum HelmType {
    HELM_ERROR("helm_release_error"),
    HELM_RELEASE_PRE_INSTALL("helm_release_pre_install"),
    HELM_INSTALL_RELEASE("helm_install_release"),
    HELM_RELEASE_HOOK_LOGS("helm_release_hook_get_logs"),
    HELM_RELEASE_UPGRADE("helm_release_upgrade"),
    HELM_RELEASE_ROLLBACK("helm_release_rollback"),
    HELM_RELEASE_START("helm_release_start"),
    HELM_RELEASE_STOP("helm_release_stop"),
    HELM_RELEASE_DELETE("helm_release_delete"),
    HELM_RELEASE_PRE_UPGRADE("helm_release_pre_upgrade"),
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
    JOB_EVENT("job_event"),
    RELEASE_POD_EVENT("release_pod_event");

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

    @JsonCreator
    public static HelmType forValue(String value) {
        return valuesMap.get(value);
    }

    /**
     * 根据string类型返回枚举类型
     *
     * @param value String
     */
    public static HelmType forString(String value) {
        switch (value) {
            case "helm_release_error":
                return HelmType.HELM_ERROR;
            case "helm_release_pre_install":
                return HelmType.HELM_RELEASE_PRE_INSTALL;
            case "helm_install_release":
                return HelmType.HELM_INSTALL_RELEASE;
            case "helm_release_hook_get_logs":
                return HelmType.HELM_RELEASE_HOOK_LOGS;
            case "helm_release_upgrade":
                return HelmType.HELM_RELEASE_UPGRADE;
            case "helm_release_rollback":
                return HelmType.HELM_RELEASE_ROLLBACK;
            case "helm_release_start":
                return HelmType.HELM_RELEASE_START;
            case "helm_release_stop":
                return HelmType.HELM_RELEASE_STOP;
            case "helm_release_delete":
                return HelmType.HELM_RELEASE_DELETE;
            case "helm_release_pre_upgrade":
                return HelmType.HELM_RELEASE_PRE_UPGRADE;
            case "network_service":
                return HelmType.NETWORK_SERVICE;
            case "network_ingress":
                return HelmType.NETWORK_INGRESS;
            case "network_service_delete":
                return HelmType.NETWORK_SERVICE_DELETE;
            case "network_ingress_delete":
                return HelmType.NETWORK_INGRESS_DELETE;
            case "resource_update":
                return HelmType.RESOURCE_UPDATE;
            case "resource_delete":
                return HelmType.RESOURCE_DELETE;
            case "kubernetes_get_logs":
                return HelmType.KUBERNETES_GET_LOGS;
            case "helm_releases":
                return HelmType.HELM_RELEASES;
            case "helm_release_install_failed":
                return HelmType.HELM_RELEASE_INSTALL_FAILED;
            case "helm_release_upgrade_failed":
                return HelmType.HELM_RELEASE_UPGRADE_FAILED;
            case "helm_release_rollback_failed":
                return HelmType.HELM_RELEASE_ROLLBACK_FAILED;
            case "helm_release_start_failed":
                return HelmType.HELM_RELEASE_START_FAILED;
            case "helm_release_stop_failed":
                return HelmType.HELM_RELEASE_STOP_FAILED;
            case "helm_release_delete_failed":
                return HelmType.HELM_RELEASE_DELETE_FAILED;
            case "network_service_update":
                return HelmType.NETWORK_SERVICE_UPDATE;
            case "helm_release_get_content":
                return HelmType.HELM_RELEASE_GET_CONTENT;
            case "helm_release_get_content_failed":
                return HelmType.HELM_RELEASE_GET_CONTENT_FAILED;
            case "command_not_send":
                return HelmType.COMMAND_NOT_SEND;
            case "network_service_failed":
                return HelmType.NETWORK_SERVICE_FAILED;
            case "network_service_delete_failed":
                return HelmType.NETWORK_SERVICE_DELETE_FAILED;
            case "network_ingress_failed":
                return HelmType.NETWORK_INGRESS_FAILED;
            case "network_ingress_delete_failed":
                return HelmType.NETWORK_INGRESS_DELETE_FAILED;
            case "resource_sync":
                return HelmType.RESOURCE_SYNC;
            case "job_event":
                return HelmType.JOB_EVENT;
            case "release_pod_event":
                return HelmType.RELEASE_POD_EVENT;
            default:
                break;
        }
        return null;
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
