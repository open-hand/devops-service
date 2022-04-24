package io.choerodon.devops.infra.enums;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2022/3/24 17:23
 */
public enum CiPipelineScheduleTriggerTypeEnum {

    /**
     * 周期执行
     */
    PERIOD("period"),
    /**
     * 单词执行
     */
    SINGLE("single");

    private final String value;

    CiPipelineScheduleTriggerTypeEnum(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }
}
