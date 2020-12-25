package io.choerodon.devops.infra.enums;

/**
 * Created by wangxiang on 2020/12/3
 * 使用类型的枚举
 */
public enum UseRecordType {
    /**
     * 部署
     */
    DEPLOY("deploy");

    private final String value;

    UseRecordType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

}
