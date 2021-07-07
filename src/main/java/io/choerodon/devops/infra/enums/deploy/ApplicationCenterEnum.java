package io.choerodon.devops.infra.enums.deploy;

/**
 * Created by wangxiang on 2021/6/29
 */
public enum ApplicationCenterEnum {

    SHARE("share"),
    PROJECT("project"),
    MARKET("market");
    public String value;

    ApplicationCenterEnum(String value) {
        this.value = value;
    }

    public String value() {
        return this.value;
    }
}
