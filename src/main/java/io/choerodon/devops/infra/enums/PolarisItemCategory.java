package io.choerodon.devops.infra.enums;

import com.fasterxml.jackson.annotation.JsonCreator;

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

    @SuppressWarnings("unchecked")
    private static final JacksonJsonEnumHelper<PolarisItemCategory> enumHelper = new JacksonJsonEnumHelper(PolarisItemCategory.class);


    PolarisItemCategory(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @JsonCreator
    public static PolarisItemCategory forValue(String value) {
        return enumHelper.forValue(value);
    }
}
