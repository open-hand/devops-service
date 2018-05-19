package io.choerodon.devops.infra.common.util.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import io.choerodon.devops.infra.common.util.JacksonJsonEnumHelper;

public enum JobStatus {
    RUNNING,
    PENDING,
    SUCCESS,
    FAILED,
    CANCELED,
    SKIPPED;

    private static JacksonJsonEnumHelper<JobStatus> enumHelper = new JacksonJsonEnumHelper(JobStatus.class);

    JobStatus() {
    }

    @JsonCreator
    public static JobStatus forValue(String value) {
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

