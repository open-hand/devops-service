package io.choerodon.devops.infra.enums;

/**
 * @author scp
 * @date 2020/7/6
 * @description
 */
public enum HostAuthType {
    /**
     * 密码模式
     */
    ACCOUNTPASSWORD("accountPassword"),
    /**
     * 密钥模式
     * 界面上填写私钥 
     */
    PUBLICKEY("publickey");
    private String value;

    HostAuthType(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }
}
