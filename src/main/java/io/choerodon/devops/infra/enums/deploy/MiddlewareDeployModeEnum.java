package io.choerodon.devops.infra.enums.deploy;

public enum MiddlewareDeployModeEnum {
    /**
     * 单机模式
     */
    STANDALONE("standalone"),
    /**
     * 哨兵模式
     */
    SENTINEL("sentinel"),
    /**
     * 主备模式
     */
    MASTER_SLAVE("master-slave");


    MiddlewareDeployModeEnum(String value) {
        this.value = value;
    }

    private final String value;

    public String getValue() {
        return value;
    }
}
