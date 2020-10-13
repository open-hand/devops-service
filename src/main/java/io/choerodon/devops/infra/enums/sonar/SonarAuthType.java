package io.choerodon.devops.infra.enums.sonar;

/**
 * User: Mr.Wang
 * Date: 2020/4/7
 */
public enum SonarAuthType {
    USERNAME_PWD("username"),
    TOKEN("token");
    private final String value;

    SonarAuthType(String value) {
        this.value = value;
    }
    public String value() {
        return value;
    }
}
