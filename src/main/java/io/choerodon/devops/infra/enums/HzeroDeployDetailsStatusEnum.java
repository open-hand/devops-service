package io.choerodon.devops.infra.enums;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2021/7/28 11:08
 */
public enum HzeroDeployDetailsStatusEnum {

    /**
     * 未执行
     */
    CREATED("created"),
    /**
     * 部署中
     */
    DEPLOYING("deploying"),
    /**
     * 部署成功
     */
    SUCCESS("success"),
    /**
     * 部署失败
     */
    FAILED("failed");

    private String value;

    HzeroDeployDetailsStatusEnum(String value) {
        this.value = value;
    }

    public String value() {
        return this.value;
    }
}
