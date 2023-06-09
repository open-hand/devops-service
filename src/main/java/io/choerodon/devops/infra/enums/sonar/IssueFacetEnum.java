package io.choerodon.devops.infra.enums.sonar;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2023/6/9 9:52
 */
public enum IssueFacetEnum {
    SEVERITIES("severities"),
    AUTHOR("author");
    private final String value;

    IssueFacetEnum(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }
}
