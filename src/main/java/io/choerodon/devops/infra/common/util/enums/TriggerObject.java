package io.choerodon.devops.infra.common.util.enums;

/**
 * Created by Sheep on 2019/5/14.
 */
public enum TriggerObject {

    HANDLER("handler"),
    OWNER("owner"),
    SPECIFIER("specifier");

    private String object;

    TriggerObject(String object) {
        this.object = object;
    }

    public String getObject() {
        return object;
    }

}
