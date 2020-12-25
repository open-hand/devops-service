package io.choerodon.devops.infra.enums;

/**
 * Created by wangxiang on 2020/12/15
 */
public enum AppSourceType {
    /**
     * 应用服务来来自市场
     */
    MARKET("market"),
    /**
     * 应用服务来自共享
     */
    SHARE("share"),

    /**
     * 未知部署来源
     */
    UNKNOWN("unknown"),

    /**
     * 应用服务来自本项目
     */
    CURRENT_PROJECT("currentProject");

    private String value;

    AppSourceType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
