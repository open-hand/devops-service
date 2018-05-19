package io.choerodon.devops.infra.common.util.enums;

public enum ObjectType {

    INSTANCE("instance"),
    SERVICE("service"),
    INGRESS("ingress"),;


    private String objectType;

    ObjectType(String objectType) {
        this.objectType = objectType;
    }

    public String getObjectType() {
        return objectType;
    }

    public void setObjectType(String objectType) {
        this.objectType = objectType;
    }
}
