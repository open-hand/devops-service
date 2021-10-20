package io.choerodon.devops.infra.enums;

/**
 * 〈功能简述〉
 * 〈外部gitlab代码库认证方式〉
 *
 * @author wanghao
 * @since 2021/9/28 10:54
 */
public enum ExternalAppAuthTypeEnum {
    /**
     * 用户名密码认证
     */
    USERNAME_PASSWORD("username_password"),
    /**
     * access_token方式认证
     */
    ACCESS_TOKEN("access_token");

    private String value;

    ExternalAppAuthTypeEnum(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

}
