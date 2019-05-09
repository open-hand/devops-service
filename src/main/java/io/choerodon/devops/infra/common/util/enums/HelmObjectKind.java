package io.choerodon.devops.infra.common.util.enums;

import java.util.HashMap;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  17:33 2019/5/9
 * Description:
 */
public enum HelmObjectKind {
    SERVICE,
    INGRESS,
    INSTANCE,
    CONFIGMAP,
    C7NHELMRELEASE,
    CERTIFICATE,
    SECRET;

    private static HashMap<String, HelmObjectKind> valuesMap = new HashMap<>(6);

    static {
        HelmObjectKind[] var0 = values();

        for (HelmObjectKind status : var0) {
            valuesMap.put(status.toValue(), status);
        }

    }

    HelmObjectKind() {
    }

    @JsonCreator
    public static HelmObjectKind forValue(String value) {
        return valuesMap.get(value);
    }

    @JsonValue
    public String toValue() {
        return this.name().toLowerCase();
    }

    @Override
    public String toString() {
        return this.name().toLowerCase();
    }
}
