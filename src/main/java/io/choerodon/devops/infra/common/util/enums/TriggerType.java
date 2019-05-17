package io.choerodon.devops.infra.common.util.enums;

/**
 * Created by Sheep on 2019/5/14.
 */
public enum TriggerType {


    EMAIL("email"),
    SMS("sms"),
    PM("pm");

    private String type;

    TriggerType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }
}
