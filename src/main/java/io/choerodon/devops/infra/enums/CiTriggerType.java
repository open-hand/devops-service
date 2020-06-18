package io.choerodon.devops.infra.enums;

import javax.annotation.Nullable;

import org.springframework.util.StringUtils;

/**
 * ci的触发方式
 *
 * @author zmf
 * @since 2020/6/18
 */
public enum CiTriggerType {
    REFS("refs"),
    EXACT_MATCH("exact_match"),
    EXACT_EXCLUDE("exact_exclude"),
    REGEX_MATCH("regex");

    private final String value;

    CiTriggerType(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }

    @Nullable
    public static CiTriggerType forValue(String value) {
        if (!StringUtils.isEmpty(value)) {
            for (CiTriggerType type : values()) {
                if (type.value.equals(value)) {
                    return type;
                }
            }
        }
        return null;
    }
}
