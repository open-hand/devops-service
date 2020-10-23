package io.choerodon.devops.infra.enums;

public enum ClusterNodeAccountTypeEnum {
    /**
     * 账号密码类型
     */
    ACCOUNTPASSWORD("accountPassword"),
    /**
     * 密钥类型
     */
    PUBLICKEY("publickey");

    private final String type;

    ClusterNodeAccountTypeEnum(String type) {
        this.type = type;
    }

    public String value() {
        return this.type;
    }
}
