package io.choerodon.devops.infra.enums;

/**
 * @author zhaotianxin
 * @since 2019/9/12
 */
public enum AppServiceStatus {
    ENABLE("启用"),
    FAILED("失败"),
    ESTABLISH("创建中"),
    DISABLE("停用");
    private String status;
    AppServiceStatus(String status) {
        this.status = status;
    }
    public String getStatus() {
        return status;
    }
}
