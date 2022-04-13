package io.choerodon.devops.infra.enums;

public enum DockerComposeStatusEnum {

    RUNNING("running"),
    OTHER("other"),
    EXITED("exited");

    private String type;

    DockerComposeStatusEnum(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }
}
