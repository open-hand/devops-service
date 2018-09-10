package io.choerodon.devops.infra.common.util.enums;

public enum ResourceType {
    JOB("Job"),
    SERVICE("Service"),
    POD("Pod"),
    INGRESS("Ingress"),
    DEPLOYMENT("Deployment"),
    REPLICASET("ReplicaSet"),
    CONFIGMAP("ConfigMap"),
    SERVICEACCOUNT("ServiceAccount"),
    DAEMONSET("DaemonSet"),
    STATEFULSET("StatefulSet"),
    SECRET("SECRET"),
    MISSTYPE("MissType");


    private String type;

    ResourceType(String type) {
        this.type = type;
    }

    /**
     * 获取k8s对象类型
     */
    public static ResourceType forString(String type) {
        switch (type) {
            case "Job":
                return ResourceType.JOB;
            case "Service":
                return ResourceType.SERVICE;
            case "Pod":
                return ResourceType.POD;
            case "Ingress":
                return ResourceType.INGRESS;
            case "Deployment":
                return ResourceType.DEPLOYMENT;
            case "ReplicaSet":
                return ResourceType.REPLICASET;
            case "ConfigMap":
                return ResourceType.CONFIGMAP;
            case "ServiceAccount":
                return ResourceType.SERVICEACCOUNT;
            case "DaemonSet":
                return ResourceType.DAEMONSET;
            case "StatefulSet":
                return ResourceType.STATEFULSET;
            case "MissType":
                return ResourceType.MISSTYPE;
            case "Secret":
                return ResourceType.SECRET;
            default:
                break;
        }
        return null;
    }

    public String getType() {
        return type;
    }

}
