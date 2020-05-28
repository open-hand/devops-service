package io.choerodon.devops.infra.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import io.choerodon.devops.infra.util.JacksonJsonEnumHelper;

public enum JobStatus {
    CREATED,
    RUNNING,
    PENDING,
    SUCCESS,
    FAILED,
    CANCELED,
    SKIPPED,
    MANUAL;

    private static JacksonJsonEnumHelper<JobStatus> enumHelper = new JacksonJsonEnumHelper<>(JobStatus.class);

    @JsonCreator
    public static JobStatus forValue(String value) {
        return enumHelper.forValue(value);
    }

    @JsonValue
    public String toValue() {
        return (enumHelper.toString(this));
    }

    @Override
    public String toString() {
        return (enumHelper.toString(this));
    }
}

