package io.choerodon.devops.infra.enums.cd;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2020/7/3 15:11
 */
public enum CdAuditStatusEnum {
    NOT_AUDIT("not_audit"),
    AUDITING("auditing"),
    REFUSED("refused"),
    PASSED("passed");
    private String value;

    CdAuditStatusEnum(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }
}
