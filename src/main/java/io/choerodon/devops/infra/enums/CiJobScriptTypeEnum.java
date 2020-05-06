package io.choerodon.devops.infra.enums;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nullable;

/**
 * ci job的 脚本类型 枚举类
 */
public enum CiJobScriptTypeEnum {
    GO("go"),
    NPM("npm"),
    MAVEN("maven"),
    UPLOAD("upload"),
    DOCKER("docker"),
    CHART("chart");

    private String type;
    private static final Map<String, CiJobScriptTypeEnum> enumMap;

    static {
        enumMap = new HashMap<>();
        for (CiJobScriptTypeEnum value : CiJobScriptTypeEnum.values()) {
            enumMap.put(value.getType(), value);
        }
    }

    CiJobScriptTypeEnum(String type) {
        this.type = type;
    }

    public String getType() {
        return this.type;
    }

    @Nullable
    public static CiJobScriptTypeEnum forType(@Nullable String type) {
        return enumMap.get(type);
    }
}
