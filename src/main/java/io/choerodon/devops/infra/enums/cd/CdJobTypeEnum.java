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
    CD_AUDIT("cd_audit"),
    /**
     * chart部署类型
     */
    CD_CHART_DEPLOY("cd_chart_deploy");

    private final String value;

    CdJobTypeEnum(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }

}
