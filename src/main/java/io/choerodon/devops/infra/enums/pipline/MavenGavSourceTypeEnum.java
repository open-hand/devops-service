package io.choerodon.devops.infra.enums.pipline;

public enum MavenGavSourceTypeEnum {

    POM("pom"),
    CUSTOM("custom");

    private String value;

    MavenGavSourceTypeEnum(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }
}
