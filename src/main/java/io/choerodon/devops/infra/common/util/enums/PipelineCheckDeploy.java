package io.choerodon.devops.infra.common.util.enums;

import java.util.HashMap;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  11:28 2019/5/20
 * Description:
 */
public enum PipelineCheckDeploy {
    PERMISSION,
    VERSIONS,
    SUCCESS;
    private static HashMap<String, PipelineCheckDeploy> valuesMap = new HashMap<>(6);

    static {
        PipelineCheckDeploy[] var0 = values();

        for (PipelineCheckDeploy status : var0) {
            valuesMap.put(status.toValue(), status);
        }

    }

    PipelineCheckDeploy() {
    }

    @JsonCreator
    public static PipelineCheckDeploy forValue(String value) {
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
