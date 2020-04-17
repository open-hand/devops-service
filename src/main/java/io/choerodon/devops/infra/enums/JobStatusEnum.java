package io.choerodon.devops.infra.enums;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @Date 2020/4/8 11:56
 */
public enum JobStatusEnum {

    CREATED("created"),
    PENDING("pending"),
    RUNNING("running"),
    FAILED("failed"),
    SUCCESS("success"),
    CANCELED("canceled"),
    SKIPPED("skipped"),
    MANUAL("manual");

    private String value;
    JobStatusEnum(String value) {
        this.value = value;
    }
    public String value() {
        return this.value;
    }
}
