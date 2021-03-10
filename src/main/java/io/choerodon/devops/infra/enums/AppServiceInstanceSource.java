package io.choerodon.devops.infra.enums;

/**
 * @author zmf
 * @since 2020/12/16
 */
public enum AppServiceInstanceSource {
    /**
     * 项目下以及共享的服务部署
     */
    NORMAL("normal"),
    /**
     * 应用市场的服务部署
     */
    MARKET("market"),
    /**
     * 市场应用的中间件
     */
    MIDDLEWARE("middleware");
    private final String value;

    AppServiceInstanceSource(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
