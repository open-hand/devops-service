package io.choerodon.devops.infra.common.util.enums;

public enum ObjectType {

    INSTANCE("instance"),
    SERVICE("service"),
    INGRESS("ingress"),
    CERTIFICATE("certificate"),
    CONFIGMAP("configMap");


    private String type;

    ObjectType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }
}
