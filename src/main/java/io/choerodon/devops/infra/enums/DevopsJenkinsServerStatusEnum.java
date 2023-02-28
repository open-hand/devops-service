package io.choerodon.devops.infra.enums;

public enum DevopsJenkinsServerStatusEnum {
    SUCCESS("success"),
    FAILED("failed"),
    ENABLED("enabled"),
    DISABLE("disable");

    private String status;

    DevopsJenkinsServerStatusEnum(String status) {
        this.status = status;
    }

    public String getStatus() {
        return this.status;
    }

}
