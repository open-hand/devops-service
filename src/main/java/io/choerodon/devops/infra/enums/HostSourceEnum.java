package io.choerodon.devops.infra.enums;

/**
 * Created by wangxiang on 2020/9/16
 */
public enum HostSourceEnum {
    /**
     * 存在主机
     */
    EXISTHOST("existHost"),
    /**
     * 自定义主机
     */
    CUSTOMHOST("customHost");

    private String value;

    HostSourceEnum(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
