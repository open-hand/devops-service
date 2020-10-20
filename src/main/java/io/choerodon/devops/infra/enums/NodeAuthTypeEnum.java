package io.choerodon.devops.infra.enums;

public enum NodeAuthTypeEnum {
    /**
     * 账号密码类型
     */
    ACCOUNT_PASSWORD("accountPassword"),
    /**
     * 密钥类型
     */
    PUBLICKEY("publickey");

    private String type;

    NodeAuthTypeEnum(String type) {
        this.type = type;
    }

    public String getType() {
        return this.type;
    }


}
