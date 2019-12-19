package io.choerodon.devops.infra.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum PvStatus {
    OPERATING("Operating"),
    DELETING("Deleting"),
    FAILED("Failed"),


    PENDING("Pending"),
    BOUND("Bound"),
    TERMINATING("Terminating"),
    AVAILABLE("Available"),
    RELEASED("Released");

    private String status;

    PvStatus(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }

    @SuppressWarnings("unchecked")
    private static final JacksonJsonEnumHelper<PvStatus> enumHelper = new JacksonJsonEnumHelper(PvStatus.class);

    @JsonCreator
    public static PvStatus forValue(String value) {
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
