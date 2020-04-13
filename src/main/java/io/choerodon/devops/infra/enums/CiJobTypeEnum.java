package io.choerodon.devops.infra.enums;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @Date 2020/4/3 16:57
 */
public enum CiJobTypeEnum {
    BUILD("build"),
    SONAR("sonar");

    private String value;

    CiJobTypeEnum(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }
}
