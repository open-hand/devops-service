package io.choerodon.devops.infra.enums;

/**
 * app查询条件，环境或者主机
 * @Author: shanyu
 * @DateTime: 2021-08-18 16:12
 **/
public enum DevopsDeployAppCenterTypeEnum {
    /**
     * 环境
     */
    ENV("env"),
    /**
     * 主机
     */
    HOST("host");

    private final String value;

    DevopsDeployAppCenterTypeEnum(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
