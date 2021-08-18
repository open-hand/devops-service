package io.choerodon.devops.infra.enums;

/**
 * @Author: scp
 * @Description:
 * @Date: Created in 2021/8/18
 * @Modified By:
 */
public enum AppCenterChartSourceEnum {
    /**
     * 应用市场
     */
    MARKET("market"),
    /**
     * 应用服务来自共享
     */
    SHARE("share"),

    /**
     * 应用服务来自本项目
     */
    NORMAL("normal"),

    /**
     * hzero应用
     */
    HZERO("hzero");

    private String value;

    AppCenterChartSourceEnum(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
