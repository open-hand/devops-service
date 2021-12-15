package io.choerodon.devops.infra.enums;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2021/12/15 16:04
 */
public enum CiJobTypeEnum {
    /**
     * 普通任务类型
     */
    NORMAL("normal"),
    /**
     * 脚本任务类型
     */
    SCRIPT("script");

    private final String value;

    CiJobTypeEnum(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }

}
