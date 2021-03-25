package io.choerodon.devops.infra.enums;

/**
 * Created by wangxiang on 2021/3/25
 */
public enum ImageSecurityEnum {
    UNKNOWN("UNKNOWN"),
    LOW("LOW"),
    MEDIUM("MEDIUM"),
    HIGH("HIGH"),
    CRITICAL("HIGH");


    private String value;

    ImageSecurityEnum(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

}
