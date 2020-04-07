package io.choerodon.devops.infra.enums;

/**
 * User: Mr.Wang
 * Date: 2020/4/7
 */
public enum SonarAuthType {
    USERNAME_PWD("username"),
    TOKEN("token");
    private String value;

    SonarAuthType(String value) {
        this.value = value;
    }
}
