package io.choerodon.devops.infra.enums.deploy;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2020/10/20 10:39
 */
public enum DeployObjectTypeEnum {

    APP("app"),
    JAR("jar"),
    IMAGE("image"),
    MIDDLEWARE("middleware"),
    HZERO("hzero");

    private String value;

    DeployObjectTypeEnum(String value) {
        this.value = value;
    }

    public String value() {
        return this.value;
    }
}
