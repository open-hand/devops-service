package io.choerodon.devops.infra.enums;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2021/12/15 16:04
 */
public enum CiCommandTypeEnum {

    /**
     * 人工卡点
     */
    AUDIT("audit"),
    /**
     * chart部署
     */
    CHART_DEPLOY("chart_deploy"),
    /**
     * 部署组部署
     */
    DEPLOYMENT_DEPLOY("deployment_deploy");

    private final String value;

    CiCommandTypeEnum(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }

}
