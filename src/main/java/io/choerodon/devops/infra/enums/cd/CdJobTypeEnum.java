package io.choerodon.devops.infra.enums.cd;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2021/12/15 16:04
 */
public enum CdJobTypeEnum {

    /**
     * 人工卡点类型
     */
    AUDIT("audit"),
    /**
     * chart部署类型
     */
    CHART_DEPLOY("chart_deploy");

    private final String value;

    CdJobTypeEnum(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }

}
