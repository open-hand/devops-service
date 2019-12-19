package io.choerodon.devops.infra.enums;

/**
 * @author zhaotianxin
 * @since 2019/10/22
 */
public enum AppServiceType {
    NORMAL_SERVICE("normal_service"),
    SHARE_SERVICE("share_service"),
    MARKET_SERVICE("market_service");
    private String type;
    AppServiceType(String type){this.type = type;}

    public String getType() {
        return type;
    }
}
