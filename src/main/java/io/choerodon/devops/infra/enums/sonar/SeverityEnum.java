package io.choerodon.devops.infra.enums.sonar;

/**
 * Created by Sheep on 2019/5/6.
 */
public enum SeverityEnum {

    MAJOR("MAJOR"),
    INFO("INFO"),
    MINOR("MINOR"),
    CRITICAL("CRITICAL"),
    BLOCKER("BLOCKER");

    private String value;

    SeverityEnum(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }
}
