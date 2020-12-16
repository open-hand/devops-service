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
     * 应用服务来自非市场的平台
     */
    PLATFORM("platform");
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
