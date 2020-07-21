package io.choerodon.devops.infra.enums;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nullable;

/**
 * ci job的 脚本类型 枚举类
 */
public enum CiJobScriptTypeEnum {
    /**
     * npm构建
     */
    NPM("npm"),
    /**
     * maven构建
     */
    MAVEN("maven"),
    /**
     * 上传软件包到存储库
     */
    UPLOAD("upload"),
    /**
     * docker构建
     */
    DOCKER("docker"),
    /**
     * 上传软件包到制品库
     */
    UPLOAD_JAR("upload_jar"),
    /**
     * maven发布
     */
    MAVEN_DEPLOY("maven_deploy");

    private final String type;
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
