package io.choerodon.devops.infra.enums;

import java.util.HashMap;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import io.choerodon.devops.api.vo.kubernetes.MemberHelper;


/**
 * Created by Zenger on 2017/11/14.
 */
public enum AccessLevel {
    NONE(0),
    GUEST(10),
    REPORTER(20),
    DEVELOPER(30),
    MASTER(40),
    OWNER(50);

    private static HashMap<Integer, AccessLevel> valuesMap = new HashMap<>(6);

    static {
        AccessLevel[] var0 = values();

        for (AccessLevel accessLevel : var0) {
            valuesMap.put(accessLevel.value, accessLevel);
        }

    }

    public final Integer value;

    AccessLevel(int value) {
        this.value = value;
    }

    @JsonCreator
    public static AccessLevel forValue(Integer value) {
        return valuesMap.get(value);
    }

    /**
     * 根据string类型返回枚举类型
     *
     * @param value String
     */
    public static AccessLevel forString(String value, MemberHelper memberHelper) {
        LabelType gitlabRoleLabel = LabelType.forValue(value);
        if (gitlabRoleLabel != null) {
            switch (gitlabRoleLabel) {
                case GITLAB_PROJECT_OWNER:
                    memberHelper.setProjectOwnerAccessLevel(AccessLevel.OWNER);
                    return AccessLevel.OWNER;
                case GITLAB_PROJECT_DEVELOPER:
                    memberHelper.setProjectDevelopAccessLevel(AccessLevel.DEVELOPER);
                    return AccessLevel.DEVELOPER;
                default:
                    return AccessLevel.NONE;
            }
        }
        return AccessLevel.NONE;
    }

    @JsonValue
    public Integer toValue() {
        return this.value;
    }

    @Override
    public String toString() {
        return this.value.toString();
    }

    public static String getAccessLevelName(Integer value) {
        AccessLevel accessLevel = AccessLevel.forValue(value);
        String accessLevelName;
        switch (accessLevel) {
            case NONE:
                accessLevelName = "NONE";
                break;
            case GUEST:
                accessLevelName = "Guest";
                break;
            case REPORTER:
                accessLevelName = "Reporter";
                break;
            case DEVELOPER:
                accessLevelName = "Developer";
                break;
            case MASTER:
                accessLevelName = "Maintainer";
                break;
            case OWNER:
                accessLevelName = "Owner";
                break;
            default:
                accessLevelName = "NONE";
        }
        return accessLevelName;
    }
}
