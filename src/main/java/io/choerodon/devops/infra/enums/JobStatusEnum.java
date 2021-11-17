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
    public static Boolean execEnd(String status) {
        if (JobStatusEnum.FAILED.value().equals(status)
                || JobStatusEnum.SUCCESS.value().equals(status)
                || JobStatusEnum.CANCELED.value().equals(status)
                || JobStatusEnum.SKIPPED.value().equals(status)
                || JobStatusEnum.MANUAL.value().equals(status)) {
            return true;
        }
        return false;
    }
}
