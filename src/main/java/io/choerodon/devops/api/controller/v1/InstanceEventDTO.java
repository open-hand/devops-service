package io.choerodon.devops.api.controller.v1;

public class InstanceEventDTO {
    private String name;
    private String event;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEvent() {
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
    }

}
