package io.choerodon.devops.infra.enums;

/**
 * 制品来源
 * @Author: shanyu
 * @DateTime: 2021-08-18 16:12
 **/
public enum DevopsDeployAppCenterProductSourceEnum {
    /**
     * 项目服务
     */
    PROJECT("project"),
    /**
     * 项目制品库
     */
    PRODUCT_LIBRARY("productLibrary"),
    /**
     * 共享服务
     */
    SHARE("share"),
    /**
     * 市场服务
     */
    MARKET("market"),
    /**
     * HZERO服务
     */
    HZERO("hzero"),
    /**
     * 所有来源
     */
    ALL("all");

    private final String value;

    DevopsDeployAppCenterProductSourceEnum(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
