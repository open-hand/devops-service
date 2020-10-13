package io.choerodon.devops.infra.enums;

/**
 * @author scp
 * @date 2020/7/6
 * @description
 */
public enum CdHostAccountType {
    /**
     * 密码模式
     */
    ACCOUNTPASSWORD("accountPassword"),
    /**
     * 密钥模式
     */
    PUBLICKEY("publickey");
    private String value;

    CdHostAccountType(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }
}
