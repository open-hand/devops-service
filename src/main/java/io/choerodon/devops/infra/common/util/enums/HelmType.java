package io.choerodon.devops.infra.common.util.enums;

import java.util.HashMap;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Created by Zenger on 2017/11/14.
 */
public enum HelmType {
    HelmError("helm_release_error"),
    HelmReleasePreInstall("helm_release_pre_install"),
    HelmInstallRelease("helm_install_release"),
    HelmReleaseHookLogs("helm_release_hook_get_logs"),
    HelmReleaseUpgrade("helm_release_upgrade"),
    HelmReleaseRollback("helm_release_rollback"),
    HelmReleaseStart("helm_release_start"),
    HelmReleaseStop("helm_release_stop"),
    HelmReleaseDelete("helm_release_delete"),
    HelmReleasePreUpgrade("helm_release_pre_upgrade"),
    NetworkService("network_service"),
    NetworkIngress("network_ingress"),
    NetworkServiceDelete("network_service_delete"),
    NetworkIngressDelete("network_ingress_delete"),
    ResourceUpdate("resource_update"),
    HelmReleases("helm_releases"),
    ResourceDelete("resource_delete"),
    HelmReleaseInstallFailed("helm_release_install_failed"),
    HelmReleaseUpgradeFailed("helm_release_upgrade_failed"),
    HelmReleaseRollbackFailed("helm_release_rollback_failed"),
    HelmReleaseStartFailed("helm_release_start_failed"),
    HelmReleaseStopFailed("helm_release_stop_failed"),
    HelmReleaseDeleteFailed("helm_release_delete_failed"),
    KubernetesGetLogs("kubernetes_get_logs"),
    HelmReleaseGetContent("helm_release_get_content"),
    HelmReleaseGetContentFailed("helm_release_get_content_failed"),
    CommandNotSend("command_not_send"),
    NetworkServiceUpdate("network_service_update");
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
                return HelmType.HelmError;
            case "helm_release_pre_install":
                return HelmType.HelmReleasePreInstall;
            case "helm_install_release":
                return HelmType.HelmInstallRelease;
            case "helm_release_hook_get_logs":
                return HelmType.HelmReleaseHookLogs;
            case "helm_release_upgrade":
                return HelmType.HelmReleaseUpgrade;
            case "helm_release_rollback":
                return HelmType.HelmReleaseRollback;
            case "helm_release_start":
                return HelmType.HelmReleaseStart;
            case "helm_release_stop":
                return HelmType.HelmReleaseStop;
            case "helm_release_delete":
                return HelmType.HelmReleaseDelete;
            case "helm_release_pre_upgrade":
                return HelmType.HelmReleasePreUpgrade;
            case "network_service":
                return HelmType.NetworkService;
            case "network_ingress":
                return HelmType.NetworkIngress;
            case "network_service_delete":
                return HelmType.NetworkServiceDelete;
            case "network_ingress_delete":
                return HelmType.NetworkIngressDelete;
            case "resource_update":
                return HelmType.ResourceUpdate;
            case "resource_delete":
                return HelmType.ResourceDelete;
            case "kubernetes_get_logs":
                return HelmType.KubernetesGetLogs;
            case "helm_releases":
                return HelmType.HelmReleases;
            case "helm_release_install_failed":
                return HelmType.HelmReleaseInstallFailed;
            case "helm_release_upgrade_failed":
                return HelmType.HelmReleaseUpgradeFailed;
            case "helm_release_rollback_failed":
                return HelmType.HelmReleaseRollbackFailed;
            case "helm_release_start_failed":
                return HelmType.HelmReleaseStartFailed;
            case "helm_release_stop_failed":
                return HelmType.HelmReleaseStopFailed;
            case "helm_release_delete_failed":
                return HelmType.HelmReleaseDeleteFailed;
            case "network_service_update":
                return HelmType.NetworkServiceUpdate;
            case "helm_release_get_content":
                return HelmType.HelmReleaseGetContent;
            case "helm_release_get_content_failed":
                return HelmType.HelmReleaseGetContentFailed;
            case "command_not_send":
                return HelmType.CommandNotSend;
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
        return this.value.toString();
    }
}
