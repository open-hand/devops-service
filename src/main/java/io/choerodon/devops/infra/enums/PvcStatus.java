package io.choerodon.devops.infra.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum PvcStatus {
    OPERATING("operating"),
    PENDING("pending"),
    BOUND("bound"),
    LOST("lost"),
    TERMINATING("terminating");

    private String status;

    PvcStatus(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }

    @SuppressWarnings("unchecked")
    private static final JacksonJsonEnumHelper<PvcStatus> enumHelper = new JacksonJsonEnumHelper(PvcStatus.class);

    @JsonCreator
    public static PvcStatus forValue(String value) {
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
