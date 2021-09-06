package io.choerodon.devops.infra.enums.deploy;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2021/8/20 11:28
 */
public enum RdupmTypeEnum {
    /**
     * chart包
     */
    CHART("chart"),
    /**
     * deployment
     */
    DEPLOYMENT("deployment"),
    /**
     * docker镜像
     */
    DOCKER("docker"),
    /**
     * jar包
     */
    JAR("jar"),
    /**
     * 其他类型
     */
    OTHER("other"),
    /**
     * 中间件
     */
    MIDDLEWARE("middleware");

    private String value;

    RdupmTypeEnum(String value) {
        this.value = value;
    }

    public String value() {
        return this.value;
    }
}
