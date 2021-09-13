package io.choerodon.devops.infra.enums.deploy;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2020/10/20 10:39
 */
public enum DeployObjectTypeEnum {

    /**
     * chart包
     */
    CHART("chart"),
    /**
     * 部署组
     */
    DEPLOYMENT("deployment"),
    JAR("jar"),
    IMAGE("image"),
    MIDDLEWARE("middleware"),
    /**
     * 其他类型
     */
    OTHER("other"),
    HZERO("hzero");

    private String value;

    DeployObjectTypeEnum(String value) {
        this.value = value;
    }

    public String value() {
        return this.value;
    }
}
