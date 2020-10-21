package io.choerodon.devops.infra.enums;

public enum ClusterNodeAuthTypeEnum {
    /**
     * 账号密码类型
     */
    ACCOUNT_PASSWORD("accountPassword"),
    /**
     * 密钥类型
     */
    PUBLICKEY("publickey");

    private final String type;

    ClusterNodeAuthTypeEnum(String type) {
        this.type = type;
    }

    public String value() {
        return this.type;
    }
}
