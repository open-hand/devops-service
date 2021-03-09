package io.choerodon.devops.infra.enums.maven;

/**
 * Created by wangxiang on 2021/3/8
 */
public enum VersionPolicyEnum {
    MIXED("MIXED"),
    SNAPSHOT("SNAPSHOT"),
    RELEASE("RELEASE");

    private String type;

    VersionPolicyEnum(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

}
