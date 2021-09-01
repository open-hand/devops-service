package io.choerodon.devops.infra.enums.deploy;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2021/8/20 11:28
 */
public enum DeployTypeEnum {
    /**
     * 新建应用
     */
    CREATE("create"),
    /**
     * 更新应用
     */
    UPDATE("update");

    private String value;

    DeployTypeEnum(String value) {
        this.value = value;
    }

    public String value() {
        return this.value;
    }
}
