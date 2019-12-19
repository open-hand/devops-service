package io.choerodon.devops.infra.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import io.choerodon.devops.infra.util.JacksonJsonEnumHelper;

public enum CommandType {

    CREATE("create"),
    STOP("stop"),
    RESTART("restart"),
    DELETE("delete"),
    UPDATE("update"),
    SYNC("sync");

    private String type;

    CommandType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    @SuppressWarnings("unchecked")
    private static final JacksonJsonEnumHelper<CommandType> enumHelper = new JacksonJsonEnumHelper(CommandType.class);

    @JsonCreator
    public static CommandType forValue(String value) {
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
