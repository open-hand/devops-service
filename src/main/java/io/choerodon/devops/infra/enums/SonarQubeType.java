package io.choerodon.devops.infra.enums;

import java.util.HashMap;

import com.fasterxml.jackson.annotation.JsonCreator;

/**
 * Created by Sheep on 2019/5/6.
 */
public enum SonarQubeType {


    QUALITY_GATE_DETAILS("quality_gate_details"),
    BUGS("bugs"),
    VULNERABILITIES("vulnerabilities"),
    NEW_VULNERABILITIES("new_vulnerabilities"),
    SQALE_INDEX("sqale_index"),
    CODE_SMELLS("code_smells"),
    NEW_CODE_SMELLS("new_code_smells"),
    NEW_TECHNICAL_DEBT("new_technical_debt"),
    COVERAGE("coverage"),
    TESTS("tests"),
    NEW_COVERAGE("new_coverage"),
    DUPLICATED_LINES_DENSITY("duplicated_lines_density"),
    DUPLICATED_BLOCKS("duplicated_blocks"),
    NEW_DUPLICATED_LINES_DENSITY("new_duplicated_lines_density"),
    NCLOC("ncloc"),
    NCLOC_LANGUAGE_DISTRIBUTION("ncloc_language_distribution"),
    UNCOVERED_LINES("uncovered_lines"),
    LINES_TO_COVER("lines_to_cover"),
    DUPLICATED_LINES("duplicated_lines"),
    NEW_BUGS("new_bugs");

    private static HashMap<String, SonarQubeType> valuesMap = new HashMap<>(6);

    static {
        SonarQubeType[] var0 = values();

        for (SonarQubeType sonarQubeType : var0) {
            valuesMap.put(sonarQubeType.type, sonarQubeType);
        }

    }

    private String type;

    SonarQubeType(String type) {
        this.type = type;
    }

    /**
     * 根据string类型返回枚举类型
     *
     * @param value String
     */
    @JsonCreator
    public static SonarQubeType forValue(String value) {
        return valuesMap.get(value);
    }

    public String getType() {
        return type;
    }
}
