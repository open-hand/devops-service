package io.choerodon.devops.infra.enums;

import java.util.HashMap;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  11:08 2019/6/6
 * Description:
 */
public enum PipelineNoticeType {
    PIPELINEFAILED,
    PIPELINESUCCESS,
    PIPELINEAUDIT,
    PIPELINESTOP,
    PIPELINEPASS;
    private static HashMap<String, PipelineNoticeType> valuesMap = new HashMap<>(6);

    static {
        PipelineNoticeType[] var0 = values();

        for (PipelineNoticeType status : var0) {
            valuesMap.put(status.toValue(), status);
        }

    }

    PipelineNoticeType() {
    }

    @JsonCreator
    public static PipelineNoticeType forValue(String value) {
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
