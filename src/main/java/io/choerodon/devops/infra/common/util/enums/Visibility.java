package io.choerodon.devops.infra.common.util.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Created by younger on 2018/4/2.
 */
public enum Visibility {
    PUBLIC,
    PRIVATE,
    INTERNAL;

    private static JacksonJsonEnumHelper<Visibility> enumHelper = new JacksonJsonEnumHelper(Visibility.class);

    Visibility() {
    }

    @JsonCreator
    public static Visibility forValue(String value) {
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
