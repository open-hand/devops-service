package io.choerodon.devops.infra.enums;

/**
 * @Author: scp
 * @Description:
 * @Date: Created in 2021/8/18
 * @Modified By:
 */
public enum AppCenterJarSourceEnum {
    /**
     * 应用市场
     */
    MARKET("market"),
    /**
     * 本地上传
     */
    LOCALHOST("localhost"),

    /**
     * 应用服务来自本项目
     */
    NORMAL("normal"),

    /**
     * hzero应用
     */
    HZERO("hzero");
    private String value;

    AppCenterJarSourceEnum(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
