package io.choerodon.devops.infra.enums;

import com.fasterxml.jackson.annotation.JsonCreator;

import java.util.HashMap;

public enum ObjectType {

    INSTANCE("instance"),
    SERVICE("service"),
    INGRESS("ingress"),
    CERTIFICATE("certificate"),
    CONFIGMAP("configMap"),
    SECRET("secret"),
    CUSTOM("custom"),
    PERSISTENTVOLUMECLAIM("PersistentVolumeClaim"),
    PERSISTENTVOLUME("PersistentVolume");

    private String type;

    private static HashMap<String, ObjectType> valuesMap = new HashMap<>(6);

    static {
        ObjectType[] var0 = values();

        for (ObjectType objectType : var0) {
            valuesMap.put(objectType.type, objectType);
        }
    }

    ObjectType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    @JsonCreator
    public static ObjectType forValue(String value) {
        return valuesMap.get(value);
    }
}
