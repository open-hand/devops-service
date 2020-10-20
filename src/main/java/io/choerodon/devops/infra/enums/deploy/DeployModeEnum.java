package io.choerodon.devops.infra.enums.deploy;

import io.kubernetes.client.models.V1Pod;

/**
 * 〈功能简述〉
 * 〈部署模式枚举〉
 *
 * @author wanghao
 * @since 2020/10/20 10:35
 */
public enum DeployModeEnum {

    ENV("env"),
    HOST("host");

    private String value;

    DeployModeEnum(String value) {
        this.value = value;
    }
    public String value() {
        return this.value;
    }
}
