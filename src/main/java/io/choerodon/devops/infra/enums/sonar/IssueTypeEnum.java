package io.choerodon.devops.infra.enums.sonar;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2023/6/9 9:52
 */
public enum IssueTypeEnum {
    BUG("BUG"),
    VULNERABILITY("VULNERABILITY"),
    CODE_SMELL("CODE_SMELL");
    private final String value;

    IssueTypeEnum(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }
}
