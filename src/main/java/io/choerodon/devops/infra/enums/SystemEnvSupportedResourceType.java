package io.choerodon.devops.infra.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * 系统环境所支持的资源类型
 *
 * @author zmf
 * @since 11/1/19
 */
public enum SystemEnvSupportedResourceType {
    C7NHELMRELEASE("C7NHelmRelease"),
    PERSISTENT_VOLUME_CLAIM("PersistentVolumeClaim"),
    PERSISTENT_VOLUME("PersistentVolume");

    @SuppressWarnings("unchecked")
    private static final JacksonJsonEnumHelper<SystemEnvSupportedResourceType> enumHelper = new JacksonJsonEnumHelper<>(SystemEnvSupportedResourceType.class);

    private String type;

    SystemEnvSupportedResourceType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    @JsonCreator
    public static SystemEnvSupportedResourceType forValue(String value) {
        return enumHelper.forValue(value);
    }

    @JsonValue
    public String toValue() {
        return enumHelper.toString(this);
    }

    @Override
    public String toString() {
        return enumHelper.toString(this);
    }
}
