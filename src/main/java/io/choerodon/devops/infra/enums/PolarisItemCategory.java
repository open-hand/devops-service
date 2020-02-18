package io.choerodon.devops.infra.enums;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Polaris检查项的类别
 *
 * @author zmf
 * @since 2/18/20
 */
public enum PolarisItemCategory {
    NETWORKING("Networking"),
    RESOURCES("Resources"),
    SECURITY("Security"),
    IMAGES("Images"),
    HEALTH_CHECK("Health Checks");

    private String value;

    private static final Map<String, PolarisItemCategory> valuesMap;

    static {
        PolarisItemCategory[] values = values();

        Map<String, PolarisItemCategory> map = new HashMap<>();

        for (PolarisItemCategory polarisItemCategory : values) {
            map.put(polarisItemCategory.value, polarisItemCategory);
        }

        valuesMap = Collections.unmodifiableMap(map);
    }

    PolarisItemCategory(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static PolarisItemCategory forValue(String value) {
        return value == null ? null : valuesMap.get(value);
    }
}
