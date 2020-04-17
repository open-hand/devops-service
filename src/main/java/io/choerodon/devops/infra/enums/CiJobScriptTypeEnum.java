package io.choerodon.devops.infra.enums;

/**
 * ci job的 脚本类型 枚举类
 */
public enum CiJobScriptTypeEnum {
    NPM("npm"),
    MAVEN("maven"),
    UPLOAD("upload"),
    DOCKER("docker"),
    CHART("chart");

    private String type;

    CiJobScriptTypeEnum(String type) {
        this.type = type;
    }

    public String getType() {
        return this.type;
    }
}
