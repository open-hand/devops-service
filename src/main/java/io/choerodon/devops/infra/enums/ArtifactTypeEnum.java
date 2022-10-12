package io.choerodon.devops.infra.enums;

public enum ArtifactTypeEnum {
    JAR("jar"),
    WAR("war");

    ArtifactTypeEnum(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    private String type;
}
