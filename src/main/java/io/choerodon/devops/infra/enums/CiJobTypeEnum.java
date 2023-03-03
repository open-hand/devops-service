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
     * 人工卡点类型
     */
    AUDIT("audit"),
    /**
     * chart部署类型
     */
    CHART_DEPLOY("chart_deploy"),
    /**
     * 部署组部署类型
     */
    DEPLOYMENT_DEPLOY("deployment_deploy"),
    /**
     * API test
     */
    API_TEST("api_test"),
    /**
     * 主机部署
     */
    HOST_DEPLOY("host_deploy"),
    /**
     * 触发其它流水线
     */
    PIPELINE_TRIGGER("pipeline_trigger"),
    /**
     * 脚本任务类型
     */
    CUSTOM("custom");

    private final String value;

    CiJobTypeEnum(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }

}
