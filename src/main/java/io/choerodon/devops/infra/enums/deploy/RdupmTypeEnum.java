package io.choerodon.devops.infra.enums.deploy;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2021/8/20 11:28
 */
public enum RdupmTypeEnum {
    JAR("jar");

    private String value;

    RdupmTypeEnum(String value) {
        this.value = value;
    }
    public String value() {
        return this.value;
    }
}
