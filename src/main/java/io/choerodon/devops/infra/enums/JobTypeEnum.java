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
     * cd 审核任务
     */
    CD_AUDIT("cdAudit"),

    /**
     * cd 主机部署任务
     */
    CD_HOST("cdHost");


    private final String value;

    JobTypeEnum(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }
}
