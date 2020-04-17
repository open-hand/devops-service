package io.choerodon.devops.infra.enums;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @Date 2020/4/3 16:57
 */
public enum CiStageTypeEnum {
    BUILD("build"),
    SONAR("sonar");

    private String value;

    CiStageTypeEnum(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }
}
