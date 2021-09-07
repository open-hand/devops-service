package io.choerodon.devops.infra.enums;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2020/4/3 16:57
 */
public enum JobTypeEnum {
    /**
     * 构建
     */
    BUILD("build"),
    /**
     * maven sonar检查
     */
    SONAR("sonar"),
    /**
     * maven chart 类型
     */
    CHART("chart"),

    /**
     * 自定义任务
     */
    CUSTOM("custom"),

    /**
     * cd 部署任务
     */
    CD_DEPLOY("cdDeploy"),

    /**
     * cd 部署组部署
     */
    CD_DEPLOYMENT("cdDeployment"),

    /**
     * cd 审核任务
     */
    CD_AUDIT("cdAudit"),

    /**
     * cd 主机部署任务
     */
    CD_HOST("cdHost"),

    /**
     * cd 外部卡点任务
     */
    CD_EXTERNAL_APPROVAL("cdExternalApproval"),

    /**
     * cd API测试任务
     */
    CD_API_TEST("cdApiTest");


    private final String value;

    JobTypeEnum(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }
}
