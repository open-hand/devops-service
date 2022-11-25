package io.choerodon.devops.infra.enums.cd;

public enum PipelineStatusEnum {
    CREATED("created"),
    RUNNING("running"),
    PENDING("pending"),
    SUCCESS("success"),
    FAILED("failed"),
    STOP("stop"),
    CANCELED("canceled"),
    NOT_AUDIT("not_audit"),
    SKIPPED("skipped");

    private final String value;

    PipelineStatusEnum(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }

}

