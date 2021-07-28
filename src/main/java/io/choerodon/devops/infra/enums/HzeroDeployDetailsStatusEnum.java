package io.choerodon.devops.infra.enums;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2021/7/28 11:08
 */
public enum HzeroDeployDetailsStatusEnum {

    CREATED("created"),
    DEPLOYING("deploying"),
    SUCCESS("success"),
    FAILED("failed");

    private String value;

    HzeroDeployDetailsStatusEnum(String value) {
        this.value = value;
    }

    public String value() {
        return this.value;
    }
}
